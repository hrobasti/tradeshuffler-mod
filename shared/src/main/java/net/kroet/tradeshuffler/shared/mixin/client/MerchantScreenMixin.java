package net.kroet.tradeshuffler.shared.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.kroet.tradeshuffler.shared.client.ClientTradeState;
import net.kroet.tradeshuffler.shared.client.PreviewRowButton;
import net.kroet.tradeshuffler.shared.client.PreviewView;
import net.kroet.tradeshuffler.shared.client.ShuffleButtonController;
import net.kroet.tradeshuffler.shared.client.ShuffleButtonHost;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the shuffle button and the trade preview to the merchant screen. The
 * preview exists only at the screen's own {@code getOffers()} call sites;
 * {@code MerchantMenu} stays untouched.
 */
@Mixin(MerchantScreen.class)
abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> implements ShuffleButtonHost {
    @Shadow
    private int shopItem;

    @Shadow
    private int scrollOff;

    @Unique
    private @Nullable SpriteIconButton tradeshuffler$shuffleButton;

    private MerchantScreenMixin(MerchantMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void tradeshuffler$forgetPreviousScreen(CallbackInfo ci) {
        ClientTradeState.clear();
        ShuffleButtonController.reset();
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void tradeshuffler$addShuffleButton(CallbackInfo ci) {
        SpriteIconButton button = ShuffleButtonController.create(pressed -> tradeshuffler$tryShuffle());
        button.visible = false;
        tradeshuffler$shuffleButton = addRenderableWidget(button);
        tradeshuffler$tickShuffleButton();
    }

    @WrapOperation(method = {"extractContents", "mouseScrolled", "mouseDragged",
            "mouseClicked"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/MerchantMenu;getOffers()Lnet/minecraft/world/item/trading/MerchantOffers;"))
    private MerchantOffers tradeshuffler$withPreview(MerchantMenu instance, Operation<MerchantOffers> original) {
        return PreviewView.combined(original.call(instance), instance);
    }

    @Inject(method = "extractContents", at = @At("TAIL"))
    private void tradeshuffler$lockPreviewRows(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        // Draw above the row items, so they are darkened too.
        graphics.nextStratum();
        for (GuiEventListener child : children()) {
            if (child instanceof PreviewRowButton row && child instanceof AbstractWidget widget) {
                int level = PreviewView.levelAt(menu, row.tradeshuffler$rowIndex() + scrollOff);
                widget.active = level == 0;
                if (level > 0 && widget.visible) {
                    PreviewView.decorateLockedRow(graphics, widget, level);
                }
            }
        }
    }

    @Inject(method = "postButtonClick", at = @At("HEAD"), cancellable = true)
    private void tradeshuffler$ignorePreviewClick(CallbackInfo ci) {
        // Second guard besides the inactive buttons: never select a preview offer.
        if (shopItem >= menu.getOffers().size()) {
            shopItem = 0;
            ci.cancel();
        }
    }

    @Override
    public void tradeshuffler$tickShuffleButton() {
        if (tradeshuffler$shuffleButton != null) {
            ShuffleButtonController.tick(tradeshuffler$shuffleButton, menu, leftPos, topPos);
        }
    }

    @Override
    public boolean tradeshuffler$tryShuffle() {
        return tradeshuffler$shuffleButton != null && ShuffleButtonController.tryShuffle(tradeshuffler$shuffleButton, menu);
    }
}
