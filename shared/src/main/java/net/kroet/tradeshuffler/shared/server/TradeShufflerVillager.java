package net.kroet.tradeshuffler.shared.server;

import net.kroet.tradeshuffler.common.rules.LevelPlan;
import net.kroet.tradeshuffler.common.rules.PlanPacer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jspecify.annotations.Nullable;

/**
 * State TradeShuffler adds to every villager (implemented by
 * {@code VillagerTradePlanMixin}). The traded flag is set by the first
 * completed trade and never cleared, not even by restocking or when the trade
 * plan is invalidated.
 */
public interface TradeShufflerVillager {
    boolean tradeshuffler$isTraded();

    void tradeshuffler$markTraded();

    /** Stored trade plan; null when none was generated or it was discarded. */
    @Nullable
    LevelPlan<MerchantOffers> tradeshuffler$getPlan();

    void tradeshuffler$setPlan(@Nullable LevelPlan<MerchantOffers> plan);

    /** When the plan may generate its next level. Not saved. */
    PlanPacer tradeshuffler$getPacer();
}
