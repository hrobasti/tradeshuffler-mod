package net.kroet.tradeshuffler.shared.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.kroet.tradeshuffler.common.config.TradeShufflerClientConfig;
import net.kroet.tradeshuffler.common.core.TradeShufflerTextKeys;
import net.kroet.tradeshuffler.shared.network.PreviewBatch;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jspecify.annotations.Nullable;

/**
 * Display-only list of the merchant screen: the real offers followed by the
 * preview offers of future levels. The menu itself is never changed, so server
 * logic, auto-fill and other mods keep seeing the real offers. Client thread
 * only.
 */
public final class PreviewView {
    /** Row geometry of vanilla's trade offer buttons. */
    private static final int ROW_WIDTH = 88;
    private static final int ROW_HEIGHT = 20;
    private static final int STRIPE_WIDTH = 2;
    /**
     * Vanilla level badge colors: apprentice (iron), journeyman (gold), expert
     * (emerald), master (diamond).
     */
    private static final int[] LEVEL_COLORS = {0, 0, 0xFFD8D8D8, 0xFFFFD83D, 0xFF17DD62, 0xFF4AEDD9};

    private PreviewView() {
    }

    /**
     * Preview batches the server sent for this menu, without levels already
     * reached.
     */
    public static List<PreviewBatch> batches(MerchantMenu menu) {
        if (!ClientConfigHolder.current().previewEnabled()) {
            return List.of();
        }
        int traderLevel = menu.getTraderLevel();
        return ClientTradeState.forContainer(menu.containerId)
                .map(state -> state.preview().stream().filter(batch -> batch.level() > traderLevel).toList())
                .orElse(List.of());
    }

    /**
     * Real offers followed by the preview offers; the real list itself when there
     * is no preview.
     */
    public static MerchantOffers combined(MerchantOffers real, MerchantMenu menu) {
        List<PreviewBatch> batches = batches(menu);
        // Without real offers the screen would treat the first preview row as the
        // selected trade (progress bar, result slot).
        if (batches.isEmpty() || real.isEmpty()) {
            return real;
        }
        MerchantOffers view = new MerchantOffers();
        view.addAll(real);
        batches.forEach(batch -> view.addAll(batch.offers()));
        return view;
    }

    /**
     * Level that unlocks the given row of the combined list; 0 for real offers or
     * rows past the end.
     */
    public static int levelAt(MerchantMenu menu, int index) {
        int previewIndex = index - menu.getOffers().size();
        if (previewIndex < 0) {
            return 0;
        }
        for (PreviewBatch batch : batches(menu)) {
            if (previewIndex < batch.offers().size()) {
                return batch.level();
            }
            previewIndex -= batch.offers().size();
        }
        return 0;
    }

    private static @Nullable MerchantOffer previewOfferAt(MerchantMenu menu, int index) {
        int previewIndex = index - menu.getOffers().size();
        if (previewIndex < 0) {
            return null;
        }
        for (PreviewBatch batch : batches(menu)) {
            if (previewIndex < batch.offers().size()) {
                return batch.offers().get(previewIndex);
            }
            previewIndex -= batch.offers().size();
        }
        return null;
    }

    /** Darkens a locked preview row and marks its level with a colored stripe. */
    public static void decorateLockedRow(GuiGraphicsExtractor graphics, AbstractWidget row, int level) {
        TradeShufflerClientConfig config = ClientConfigHolder.current();
        int x = row.getX();
        int y = row.getY();
        int alpha = config.previewDimAlpha() * 255 / 100;
        if (alpha > 0) {
            graphics.fill(x, y, x + ROW_WIDTH, y + ROW_HEIGHT, alpha << 24);
        }
        if (config.previewLevelStripe() && level < LEVEL_COLORS.length) {
            graphics.fill(x, y, x + STRIPE_WIDTH, y + ROW_HEIGHT, LEVEL_COLORS[level]);
        }
    }

    /**
     * Tooltip of a hovered preview row: the hovered item's own tooltip (so
     * enchanted books stay readable) plus the unlock level and a base-price note.
     * Item zones match vanilla's trade offer button.
     */
    public static void setPreviewTooltip(GuiGraphicsExtractor graphics, Font font, MerchantMenu menu, int index, int rowX, int mouseX, int mouseY) {
        MerchantOffer offer = previewOfferAt(menu, index);
        int level = levelAt(menu, index);
        if (offer == null || level == 0) {
            return;
        }

        ItemStack hovered = ItemStack.EMPTY;
        if (mouseX < rowX + 20) {
            hovered = offer.getCostA();
        } else if (mouseX > rowX + 30 && mouseX < rowX + 50) {
            hovered = offer.getCostB();
        } else if (mouseX > rowX + 65) {
            hovered = offer.getResult();
        }

        List<Component> lines = new ArrayList<>();
        Optional<TooltipComponent> image = Optional.empty();
        if (!hovered.isEmpty()) {
            lines.addAll(Screen.getTooltipFromItem(Minecraft.getInstance(), hovered));
            image = hovered.getTooltipImage();
        }
        lines.add(Component.translatable(TradeShufflerTextKeys.SCREEN_PREVIEW_UNLOCKS_AT, Component.translatable("merchant.level." + level), level)
                .withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable(TradeShufflerTextKeys.SCREEN_PREVIEW_BASE_PRICE).withStyle(ChatFormatting.GRAY));
        graphics.setTooltipForNextFrame(font, lines, image, mouseX, mouseY);
    }
}
