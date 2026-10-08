package net.kroet.tradeshuffler.shared.client;

import java.util.Objects;
import java.util.Optional;
import net.kroet.tradeshuffler.common.config.TradeShufflerClientConfig;
import net.kroet.tradeshuffler.common.core.TradeShufflerTextKeys;
import net.kroet.tradeshuffler.common.rules.ShuffleEligibility;
import net.kroet.tradeshuffler.shared.TradeShufflerConstants;
import net.kroet.tradeshuffler.shared.network.ShuffleRequestPayload;
import net.kroet.tradeshuffler.shared.network.TradeShufflerNetworkBindings;
import net.kroet.tradeshuffler.shared.network.TradeStatePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MerchantMenu;
import org.jspecify.annotations.Nullable;

/**
 * Creates, positions and enables the shuffle button of the merchant screen.
 * Client thread only. The button is shown whenever the server sent a trade
 * state; it is greyed out with the reason in its tooltip while shuffling is not
 * possible, and without a reason while a request awaits the server's answer.
 */
public final class ShuffleButtonController {
    private static final WidgetSprites SPRITES = new WidgetSprites(
            Identifier.fromNamespaceAndPath(TradeShufflerConstants.MOD_ID, "shuffle"),
            Identifier.fromNamespaceAndPath(TradeShufflerConstants.MOD_ID, "shuffle_disabled"));
    private static final Component TITLE = Component.translatable(TradeShufflerTextKeys.SCREEN_SHUFFLE_TOOLTIP);
    private static final Tooltip DEFAULT_TOOLTIP = Tooltip.create(TITLE);
    private static final int BUTTON_SIZE = 20;
    private static final int SPRITE_SIZE = 18;
    /** Left of the window, level with the first trade row. */
    private static final int DEFAULT_OFFSET_X = -22;
    private static final int DEFAULT_OFFSET_Y = 18;
    private static final int MIN_X = 2;
    /** A rejected request gets no answer; re-enable the button after one second. */
    private static final int PENDING_TIMEOUT_TICKS = 20;

    private static int pendingTicks;
    private static long pendingRevision;
    private static @Nullable String shownReason;

    private ShuffleButtonController() {
    }

    public static SpriteIconButton create(Button.OnPress onPress) {
        SpriteIconButton button = new ShuffleButton(BUTTON_SIZE, TITLE, SPRITE_SIZE, SPRITES, onPress);
        button.setTooltip(DEFAULT_TOOLTIP);
        shownReason = null;
        return button;
    }

    /** Called from the screen tick, never while rendering. */
    /** Forgets a pending shuffle of a previous screen. */
    public static void reset() {
        pendingTicks = 0;
    }

    public static void tick(AbstractWidget button, MerchantMenu menu, int leftPos, int topPos) {
        TradeShufflerClientConfig config = ClientConfigHolder.current();
        if (pendingTicks > 0 && (ClientTradeState.revision() != pendingRevision || --pendingTicks == 0)) {
            pendingTicks = 0;
        }

        Optional<String> blockReason = blockReason(config, menu);
        button.visible = blockReason.isPresent();
        button.active = blockReason.map(String::isEmpty).orElse(false) && pendingTicks == 0;
        updateTooltip(button, blockReason.orElse(""));
        button.setX(Math.max(MIN_X, leftPos + DEFAULT_OFFSET_X + config.shuffleButtonOffsetX()));
        button.setY(topPos + DEFAULT_OFFSET_Y + config.shuffleButtonOffsetY());
    }

    /** Sends a request if the button is usable; returns whether one was sent. */
    public static boolean tryShuffle(AbstractWidget button, MerchantMenu menu) {
        if (!button.visible || !button.active || !blockReason(ClientConfigHolder.current(), menu).map(String::isEmpty).orElse(false)) {
            return false;
        }
        TradeShufflerNetworkBindings.client().sendShuffleRequest(new ShuffleRequestPayload(menu.containerId));
        pendingRevision = ClientTradeState.revision();
        pendingTicks = PENDING_TIMEOUT_TICKS;
        button.active = false;
        return true;
    }

    /** Plays the vanilla click, for requests triggered by the key. */
    public static void playClickSound() {
        AbstractWidget.playButtonClickSound(Minecraft.getInstance().getSoundManager());
    }

    /**
     * Empty optional: hide the button. Empty string: shuffling is allowed.
     * Otherwise the reason why it is not.
     */
    private static Optional<String> blockReason(TradeShufflerClientConfig config, MerchantMenu menu) {
        if (!config.shuffleButtonEnabled() || !TradeShufflerNetworkBindings.client().serverSupportsShuffle() || !menu.showProgressBar()) {
            return Optional.empty();
        }
        // The local XP check greys the button out right after the first trade, before
        // the server's new state arrives.
        return ClientTradeState.forContainer(menu.containerId)
                .map(TradeStatePayload::shuffleBlockReason)
                .map(reason -> reason.isEmpty() && menu.getTraderXp() > 0 ? ShuffleEligibility.Verdict.ALREADY_TRADED.name() : reason);
    }

    private static void updateTooltip(AbstractWidget button, String reason) {
        if (Objects.equals(reason, shownReason)) {
            return;
        }
        shownReason = reason;
        if (reason.isEmpty()) {
            button.setTooltip(DEFAULT_TOOLTIP);
            return;
        }
        String key = TradeShufflerTextKeys.shuffleUnavailable(reason);
        Component reasonText = Component.translatable(Language.getInstance().has(key) ? key : TradeShufflerTextKeys.SCREEN_SHUFFLE_UNAVAILABLE_OTHER)
                .withStyle(ChatFormatting.GRAY);
        button.setTooltip(Tooltip.create(Component.empty().append(TITLE).append("\n").append(reasonText)));
    }
}
