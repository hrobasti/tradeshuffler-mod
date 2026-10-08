package net.kroet.tradeshuffler.shared.server;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.kroet.tradeshuffler.common.rules.GenerationBudget;
import net.kroet.tradeshuffler.common.rules.LevelPlan;
import net.kroet.tradeshuffler.shared.mixin.AbstractVillagerInvoker;
import net.kroet.tradeshuffler.shared.mixin.VillagerInvoker;
import net.kroet.tradeshuffler.shared.network.PreviewBatch;
import net.kroet.tradeshuffler.shared.network.TradeShufflerNetworkBindings;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Binding trade preview: future trade batches are generated in advance through
 * vanilla's trade set path, stored on the villager and consumed on level-up
 * instead of rolling new ones. Server thread only.
 */
public final class TradePlanService {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final GenerationBudget BUDGET = new GenerationBudget();

    private TradePlanService() {
    }

    /**
     * Schedules generation when a player who can see the preview opens the screen.
     */
    public static void onStartTrading(ServerPlayer player, Villager villager) {
        if (!ServerConfigHolder.current().previewEnabled() || !TradeShufflerNetworkBindings.server().canReceive(player)) {
            return;
        }
        LevelPlan<MerchantOffers> plan = validPlan(villager);
        if (plan == null || !plan.isComplete()) {
            schedule(villager);
        }
    }

    /** Starts (or continues) generating the plan, one level per tick. */
    public static void schedule(Villager villager) {
        if (ServerConfigHolder.current().previewEnabled() && villager.getVillagerData().level() < LevelPlan.MAX_LEVEL) {
            state(villager).tradeshuffler$getPacer().request();
        }
    }

    /** Like {@link #schedule}, but only once shuffling has paused. */
    static void scheduleAfterShuffle(Villager villager) {
        if (ServerConfigHolder.current().previewEnabled()) {
            state(villager).tradeshuffler$getPacer().requestAfterShuffle();
        }
    }

    static void reset() {
        BUDGET.reset();
    }

    /** Called from the villager tick while generation is pending. */
    public static void onTick(Villager villager, ServerLevel level) {
        if (state(villager).tradeshuffler$getPacer().tick(BUDGET, level.getServer().getTickCount())) {
            generateNext(villager, level);
        }
    }

    private static void generateNext(Villager villager, ServerLevel level) {
        TradeShufflerVillager state = state(villager);
        if (!ServerConfigHolder.current().previewEnabled()) {
            state.tradeshuffler$getPacer().cancel();
            return;
        }

        LevelPlan<MerchantOffers> plan = validPlan(villager);
        if (plan == null) {
            plan = new LevelPlan<>(stampOf(villager), villager.getVillagerData().level());
            state.tradeshuffler$setPlan(plan);
        }
        OptionalInt next = plan.nextMissingLevel();
        if (next.isPresent()) {
            plan.put(next.getAsInt(), generateBatch(villager, level, next.getAsInt()));
        }
        if (plan.isComplete()) {
            state.tradeshuffler$getPacer().cancel();
            LOGGER.debug("Trade plan complete for villager {}", villager.getUUID());
            if (villager.getTradingPlayer() instanceof ServerPlayer player) {
                TradeStateService.sendTradeState(player, villager);
            }
        }
    }

    /**
     * Hooked at the start of vanilla's {@code updateTrades}; returns true when the
     * planned batch replaced vanilla's generation.
     */
    public static boolean onUpdateTrades(Villager villager) {
        TradeShufflerVillager state = state(villager);
        MerchantOffers offers = ((AbstractVillagerInvoker) villager).tradeshuffler$getOffersField();
        // Empty offers mean a new profession or reset offers (also a shuffle): vanilla
        // rolls the novice batch and the old plan no longer applies.
        if (offers == null || offers.isEmpty()) {
            state.tradeshuffler$setPlan(null);
            return false;
        }

        int level = villager.getVillagerData().level();
        LevelPlan<MerchantOffers> plan = state.tradeshuffler$getPlan();
        if (plan != null && plan.isValidFor(stampOf(villager), level)) {
            Optional<MerchantOffers> batch = plan.take(level);
            if (batch.isPresent()) {
                offers.addAll(batch.get());
                Player tradingPlayer = villager.getTradingPlayer();
                if (tradingPlayer != null) {
                    ((VillagerInvoker) villager).tradeshuffler$invokeUpdateSpecialPrices(tradingPlayer);
                }
                resendState(villager);
                return true;
            }
        }

        // The client never saw a batch for this level, so letting vanilla roll it keeps
        // the preview binding. The rest of the plan is regenerated from the new level.
        state.tradeshuffler$setPlan(null);
        if (villager.getTradingPlayer() instanceof ServerPlayer player && TradeShufflerNetworkBindings.server().canReceive(player)) {
            schedule(villager);
        }
        resendState(villager);
        return false;
    }

    /** Preview for the trade state: only complete plans, never a partial one. */
    static List<PreviewBatch> preview(Villager villager) {
        if (!ServerConfigHolder.current().previewEnabled()) {
            return List.of();
        }
        LevelPlan<MerchantOffers> plan = validPlan(villager);
        if (plan == null || !plan.isComplete()) {
            return List.of();
        }
        List<PreviewBatch> preview = new ArrayList<>();
        plan.batches().forEach((level, offers) -> {
            if (!offers.isEmpty()) {
                preview.add(new PreviewBatch(level, offers.copy()));
            }
        });
        return preview;
    }

    /**
     * Current plan if it still fits the villager; drops levels it already reached.
     */
    private static @Nullable LevelPlan<MerchantOffers> validPlan(Villager villager) {
        TradeShufflerVillager state = state(villager);
        LevelPlan<MerchantOffers> plan = state.tradeshuffler$getPlan();
        if (plan == null) {
            return null;
        }
        int level = villager.getVillagerData().level();
        if (!plan.isValidFor(stampOf(villager), level)) {
            state.tradeshuffler$setPlan(null);
            return null;
        }
        // As in vanilla, a level set by command skips the trades of skipped levels.
        plan.dropUpTo(level);
        if (plan.fromLevel() > LevelPlan.MAX_LEVEL) {
            // A master has nothing left to preview.
            state.tradeshuffler$setPlan(null);
            return null;
        }
        return plan;
    }

    private static MerchantOffers generateBatch(Villager villager, ServerLevel level, int villagerLevel) {
        MerchantOffers batch = new MerchantOffers();
        ResourceKey<TradeSet> tradeSet = villager.getVillagerData().profession().value().getTrades(villagerLevel);
        if (tradeSet != null) {
            ((AbstractVillagerInvoker) villager).tradeshuffler$invokeAddOffersFromTradeSet(level, batch, tradeSet);
        }
        return batch;
    }

    private static void resendState(Villager villager) {
        if (villager.getTradingPlayer() instanceof ServerPlayer player) {
            TradeStateService.sendTradeState(player, villager);
        }
    }

    private static LevelPlan.Stamp stampOf(Villager villager) {
        VillagerData data = villager.getVillagerData();
        return new LevelPlan.Stamp(id(data.profession()), id(data.type()));
    }

    private static String id(Holder<?> holder) {
        return holder.unwrapKey().map(key -> key.identifier().toString()).orElse("");
    }

    private static TradeShufflerVillager state(Villager villager) {
        return (TradeShufflerVillager) villager;
    }
}
