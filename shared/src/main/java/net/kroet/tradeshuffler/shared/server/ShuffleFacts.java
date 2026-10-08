package net.kroet.tradeshuffler.shared.server;

import net.kroet.tradeshuffler.common.config.TradeShufflerServerConfig;
import net.kroet.tradeshuffler.common.rules.ShuffleEligibility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.item.trading.Merchant;
import org.jspecify.annotations.Nullable;

/**
 * Collects the facts {@link ShuffleEligibility} decides on from the live game
 * state.
 */
final class ShuffleFacts {
    private ShuffleFacts() {
    }

    static ShuffleEligibility.Input collect(ServerPlayer player, @Nullable Merchant trader, boolean containerMatches, boolean cooldownElapsed) {
        TradeShufflerServerConfig config = ServerConfigHolder.current();
        if (!(trader instanceof Villager villager)) {
            return new ShuffleEligibility.Input(
                    config.shuffleEnabled(), containerMatches, false, false, false, 0, 0, false, config.requireJobSite(), false, cooldownElapsed);
        }

        VillagerData data = villager.getVillagerData();
        return new ShuffleEligibility.Input(
                config.shuffleEnabled(),
                containerMatches,
                true,
                villager.isAlive() && villager.getTradingPlayer() == player,
                ((TradeShufflerVillager) villager).tradeshuffler$isTraded(),
                villager.getVillagerXp(),
                data.level(),
                data.profession().value().getTrades(ShuffleEligibility.NOVICE_LEVEL) != null,
                config.requireJobSite(),
                villager.getBrain().hasMemoryValue(MemoryModuleType.JOB_SITE),
                cooldownElapsed);
    }
}
