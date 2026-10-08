package net.kroet.tradeshuffler.shared.mixin;

import net.kroet.tradeshuffler.shared.server.ZombieConversion;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks the single conversion path used by zombie infection and cure.
 */
@Mixin(Mob.class)
abstract class MobConversionMixin {
    @Inject(method = "convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;", at = @At("RETURN"))
    private void tradeshuffler$carryTradeState(
            EntityType<?> entityType,
            ConversionParams params,
            EntitySpawnReason spawnReason,
            ConversionParams.AfterConversion<?> afterConversion,
            CallbackInfoReturnable<Mob> cir) {
        Mob converted = cir.getReturnValue();
        if (converted != null) {
            ZombieConversion.onConverted((Mob) (Object) this, converted);
        }
    }
}
