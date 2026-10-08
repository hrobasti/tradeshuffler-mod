package net.kroet.tradeshuffler.shared.mixin;

import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Compile-checked access to the private price update, which reapplies
 * reputation and Hero of the Village discounts and resends the offers.
 */
@Mixin(Villager.class)
public interface VillagerInvoker {
    @Invoker("updateSpecialPrices")
    void tradeshuffler$invokeUpdateSpecialPrices(Player player);
}
