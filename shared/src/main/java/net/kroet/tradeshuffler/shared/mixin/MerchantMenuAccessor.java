package net.kroet.tradeshuffler.shared.mixin;

import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Compile-checked access to the merchant behind a menu and its trade slots.
 */
@Mixin(MerchantMenu.class)
public interface MerchantMenuAccessor {
    @Accessor("trader")
    Merchant tradeshuffler$getTrader();

    @Accessor("tradeContainer")
    MerchantContainer tradeshuffler$getTradeContainer();
}
