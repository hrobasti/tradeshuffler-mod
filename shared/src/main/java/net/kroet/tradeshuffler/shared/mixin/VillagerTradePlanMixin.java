package net.kroet.tradeshuffler.shared.mixin;

import net.kroet.tradeshuffler.common.rules.LevelPlan;
import net.kroet.tradeshuffler.common.rules.PlanPacer;
import net.kroet.tradeshuffler.shared.server.TradePlanCodec;
import net.kroet.tradeshuffler.shared.server.TradePlanService;
import net.kroet.tradeshuffler.shared.server.TradeShufflerVillager;
import net.kroet.tradeshuffler.shared.server.TradeStateService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Per-villager TradeShuffler state (traded flag, trade plan), stored in the
 * villager's own save data so it works the same on both loaders. Unknown keys
 * are ignored by vanilla, so removing the mod leaves the villager intact.
 */
@Mixin(Villager.class)
abstract class VillagerTradePlanMixin implements TradeShufflerVillager {
    @Unique
    private static final String TRADED_KEY = "tradeshuffler:traded";
    @Unique
    private static final String PLAN_KEY = "tradeshuffler:trade_plan";

    @Unique
    private boolean tradeshuffler$traded;
    @Unique
    private @Nullable LevelPlan<MerchantOffers> tradeshuffler$plan;
    @Unique
    private final PlanPacer tradeshuffler$pacer = new PlanPacer();

    @Override
    public boolean tradeshuffler$isTraded() {
        return tradeshuffler$traded;
    }

    @Override
    public void tradeshuffler$markTraded() {
        tradeshuffler$traded = true;
    }

    @Override
    public @Nullable LevelPlan<MerchantOffers> tradeshuffler$getPlan() {
        return tradeshuffler$plan;
    }

    @Override
    public void tradeshuffler$setPlan(@Nullable LevelPlan<MerchantOffers> plan) {
        tradeshuffler$plan = plan;
    }

    @Override
    public PlanPacer tradeshuffler$getPacer() {
        return tradeshuffler$pacer;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void tradeshuffler$save(ValueOutput output, CallbackInfo ci) {
        if (tradeshuffler$traded) {
            output.putBoolean(TRADED_KEY, true);
        }
        if (tradeshuffler$plan != null) {
            output.store(PLAN_KEY, TradePlanCodec.CODEC, tradeshuffler$plan);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void tradeshuffler$load(ValueInput input, CallbackInfo ci) {
        tradeshuffler$traded = input.getBooleanOr(TRADED_KEY, false);
        tradeshuffler$plan = input.read(PLAN_KEY, TradePlanCodec.CODEC).orElse(null);
    }

    @Inject(method = "startTrading", at = @At("TAIL"))
    private void tradeshuffler$afterStartTrading(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            Villager villager = (Villager) (Object) this;
            TradePlanService.onStartTrading(serverPlayer, villager);
            TradeStateService.sendTradeState(serverPlayer, villager);
        }
    }

    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void tradeshuffler$consumePlannedBatch(ServerLevel level, CallbackInfo ci) {
        if (TradePlanService.onUpdateTrades((Villager) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tradeshuffler$generatePlan(CallbackInfo ci) {
        Villager villager = (Villager) (Object) this;
        if (tradeshuffler$pacer.isPending() && villager.level() instanceof ServerLevel serverLevel) {
            TradePlanService.onTick(villager, serverLevel);
        }
    }
}
