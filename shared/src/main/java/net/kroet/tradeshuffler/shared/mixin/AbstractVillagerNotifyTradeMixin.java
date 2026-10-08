package net.kroet.tradeshuffler.shared.mixin;

import net.kroet.tradeshuffler.shared.server.TradeStateService;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Locks shuffling after the first completed trade. Hooks the trade itself
 * rather than XP, so data-pack trades that give 0 XP lock it too.
 */
@Mixin(AbstractVillager.class)
abstract class AbstractVillagerNotifyTradeMixin {
    @Inject(method = "notifyTrade", at = @At("TAIL"))
    private void tradeshuffler$afterTrade(MerchantOffer offer, CallbackInfo ci) {
        if ((Object) this instanceof Villager villager && !villager.level().isClientSide()) {
            TradeStateService.onTradeCompleted(villager);
        }
    }
}
