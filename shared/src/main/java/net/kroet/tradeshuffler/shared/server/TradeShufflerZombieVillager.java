package net.kroet.tradeshuffler.shared.server;

import net.kroet.tradeshuffler.common.rules.LevelPlan;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jspecify.annotations.Nullable;

/**
 * TradeShuffler state a zombie villager keeps between infection and cure
 * (implemented by {@code ZombieVillagerTradePlanMixin}), so a cured villager
 * returns with its preview and traded flag.
 */
public interface TradeShufflerZombieVillager {
    boolean tradeshuffler$isTraded();

    void tradeshuffler$setTraded(boolean traded);

    @Nullable
    LevelPlan<MerchantOffers> tradeshuffler$getPlan();

    void tradeshuffler$setPlan(@Nullable LevelPlan<MerchantOffers> plan);
}
