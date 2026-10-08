package net.kroet.tradeshuffler.shared.mixin.client;

import net.kroet.tradeshuffler.shared.client.PreviewRowButton;
import net.kroet.tradeshuffler.shared.client.PreviewView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Marks vanilla's (private) trade offer buttons and gives preview rows their
 * tooltip; vanilla only shows tooltips for real offers.
 */
@Mixin(targets = "net.minecraft.client.gui.screens.inventory.MerchantScreen$TradeOfferButton")
abstract class MerchantScreenTradeOfferButtonMixin extends Button.Plain implements PreviewRowButton {
    @Shadow
    @Final
    private int index;

    @Shadow
    @Final
    MerchantScreen this$0;

    private MerchantScreenTradeOfferButtonMixin(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration narration) {
        super(x, y, width, height, message, onPress, narration);
    }

    @Override
    public int tradeshuffler$rowIndex() {
        return index;
    }

    @Inject(method = "extractToolTip", at = @At("TAIL"))
    private void tradeshuffler$previewTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (isHovered()) {
            int listIndex = index + ((MerchantScreenAccessor) this$0).tradeshuffler$getScrollOff();
            PreviewView.setPreviewTooltip(graphics, Minecraft.getInstance().font, this$0.getMenu(), listIndex, getX(), mouseX, mouseY);
        }
    }
}
