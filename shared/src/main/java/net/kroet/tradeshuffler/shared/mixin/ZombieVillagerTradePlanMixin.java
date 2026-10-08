package net.kroet.tradeshuffler.shared.mixin;

import net.kroet.tradeshuffler.common.rules.LevelPlan;
import net.kroet.tradeshuffler.shared.server.TradePlanCodec;
import net.kroet.tradeshuffler.shared.server.TradeShufflerZombieVillager;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
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
 * Keeps a villager's TradeShuffler state while it is a zombie villager, under
 * the same save keys as on the villager.
 */
@Mixin(ZombieVillager.class)
abstract class ZombieVillagerTradePlanMixin implements TradeShufflerZombieVillager {
    @Unique
    private static final String TRADED_KEY = "tradeshuffler:traded";
    @Unique
    private static final String PLAN_KEY = "tradeshuffler:trade_plan";

    @Unique
    private boolean tradeshuffler$traded;
    @Unique
    private @Nullable LevelPlan<MerchantOffers> tradeshuffler$plan;

    @Override
    public boolean tradeshuffler$isTraded() {
        return tradeshuffler$traded;
    }

    @Override
    public void tradeshuffler$setTraded(boolean traded) {
        tradeshuffler$traded = traded;
    }

    @Override
    public @Nullable LevelPlan<MerchantOffers> tradeshuffler$getPlan() {
        return tradeshuffler$plan;
    }

    @Override
    public void tradeshuffler$setPlan(@Nullable LevelPlan<MerchantOffers> plan) {
        tradeshuffler$plan = plan;
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
}
