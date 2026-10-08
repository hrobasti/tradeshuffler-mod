package net.kroet.tradeshuffler.shared.mixin.client;

import net.kroet.tradeshuffler.shared.client.ShuffleButtonController;
import net.kroet.tradeshuffler.shared.client.ShuffleButtonHost;
import net.kroet.tradeshuffler.shared.client.TradeShufflerKeyMappings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Screen tick and shuffle key for the merchant screen. Hooked here because
 * {@code MerchantScreen} declares neither method, and vanilla key mappings do
 * not fire while a screen is open.
 */
@Mixin(AbstractContainerScreen.class)
abstract class AbstractContainerScreenMixin {
    @Inject(method = "containerTick", at = @At("TAIL"))
    private void tradeshuffler$tickShuffleButton(CallbackInfo ci) {
        if (this instanceof ShuffleButtonHost host) {
            host.tradeshuffler$tickShuffleButton();
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void tradeshuffler$handleShuffleKey(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        KeyMapping shuffleKey = TradeShufflerKeyMappings.shuffle();
        if (this instanceof ShuffleButtonHost host && shuffleKey != null && shuffleKey.matches(event) && host.tradeshuffler$tryShuffle()) {
            ShuffleButtonController.playClickSound();
            cir.setReturnValue(true);
        }
    }
}
