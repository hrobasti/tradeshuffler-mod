package net.kroet.tradeshuffler.shared.mixin.client;

import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Compile-checked access to the scroll offset of the trade list.
 */
@Mixin(MerchantScreen.class)
public interface MerchantScreenAccessor {
    @Accessor("scrollOff")
    int tradeshuffler$getScrollOff();
}
