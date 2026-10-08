package net.kroet.tradeshuffler.shared.server;

import com.mojang.logging.LogUtils;
import net.kroet.tradeshuffler.common.rules.ShuffleEligibility;
import net.kroet.tradeshuffler.shared.mixin.MerchantMenuAccessor;
import net.kroet.tradeshuffler.shared.network.TradeShufflerNetworkBindings;
import net.kroet.tradeshuffler.shared.network.TradeStatePayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import org.slf4j.Logger;

/**
 * Sends the current trade state of a villager to the player trading with it.
 * Players without TradeShuffler on their client get nothing.
 */
public final class TradeStateService {
    private static final Logger LOGGER = LogUtils.getLogger();

    private TradeStateService() {
    }

    /**
     * Sends the state if the player's open merchant menu belongs to the villager.
     */
    public static void sendTradeState(ServerPlayer player, Villager villager) {
        if (!(player.containerMenu instanceof MerchantMenu menu) || ((MerchantMenuAccessor) menu).tradeshuffler$getTrader() != villager) {
            return;
        }
        TradeShufflerNetworkBindings.Server network = TradeShufflerNetworkBindings.server();
        if (!network.canReceive(player)) {
            return;
        }

        // Container and cooldown only matter for requests, not for showing the button.
        ShuffleEligibility.Verdict verdict = ShuffleEligibility.evaluate(ShuffleFacts.collect(player, villager, true, true));
        LOGGER.debug("Trade state for {} (container {}): {}", player.getScoreboardName(), menu.containerId, verdict);
        network.sendTradeState(player, new TradeStatePayload(menu.containerId, verdict.allowed() ? "" : verdict.name(), TradePlanService.preview(villager)));
    }

    /** Called after every completed trade with a villager on the server. */
    public static void onTradeCompleted(Villager villager) {
        TradeShufflerVillager state = (TradeShufflerVillager) villager;
        if (state.tradeshuffler$isTraded()) {
            return;
        }
        state.tradeshuffler$markTraded();
        if (villager.getTradingPlayer() instanceof ServerPlayer player) {
            sendTradeState(player, villager);
        }
    }
}
