package net.kroet.tradeshuffler.shared.server;

import com.mojang.logging.LogUtils;
import java.util.UUID;
import net.kroet.tradeshuffler.common.rules.ShuffleCooldown;
import net.kroet.tradeshuffler.common.rules.ShuffleEligibility;
import net.kroet.tradeshuffler.shared.mixin.MerchantMenuAccessor;
import net.kroet.tradeshuffler.shared.network.ShuffleRequestPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import org.slf4j.Logger;

/**
 * Validates shuffle requests on the server thread. Rejected requests are
 * dropped silently; the client never assumes success.
 */
public final class ShuffleRequestHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ShuffleCooldown<UUID> COOLDOWN = new ShuffleCooldown<>();

    private ShuffleRequestHandler() {
    }

    public static void handle(ServerPlayer player, ShuffleRequestPayload payload) {
        AbstractContainerMenu open = player.containerMenu;
        boolean containerMatches = open instanceof MerchantMenu && open.containerId == payload.containerId();
        Merchant trader = containerMatches ? ((MerchantMenuAccessor) open).tradeshuffler$getTrader() : null;
        long tick = player.level().getServer().getTickCount();
        boolean cooldownElapsed = COOLDOWN.isElapsed(player.getUUID(), tick, ServerConfigHolder.current().shuffleCooldownTicks());

        ShuffleEligibility.Verdict verdict = ShuffleEligibility.evaluate(ShuffleFacts.collect(player, trader, containerMatches, cooldownElapsed));
        if (!verdict.allowed()) {
            LOGGER.debug("Rejected shuffle request from {} (container {}): {}", player.getScoreboardName(), payload.containerId(), verdict);
            return;
        }

        COOLDOWN.markUsed(player.getUUID(), tick);
        LOGGER.debug("Shuffling trades for {} (container {})", player.getScoreboardName(), payload.containerId());
        ShuffleService.shuffle(player, (Villager) trader, (MerchantMenu) open);
    }

    /** Called when a player leaves, so the cooldown map does not grow. */
    public static void onPlayerLeft(UUID playerId) {
        COOLDOWN.forget(playerId);
    }

    /** Called when a server starts, so no cooldown carries over between worlds. */
    public static void reset() {
        COOLDOWN.clear();
    }
}
