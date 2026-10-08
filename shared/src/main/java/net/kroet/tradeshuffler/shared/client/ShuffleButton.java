package net.kroet.tradeshuffler.shared.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;

/**
 * Icon-only shuffle button. It does not keep focus after a mouse click,
 * otherwise it would stay drawn highlighted after the first shuffle; keyboard
 * navigation can still focus it.
 */
final class ShuffleButton extends SpriteIconButton.CenteredIcon {
    ShuffleButton(int size, Component message, int spriteSize, WidgetSprites sprites, Button.OnPress onPress) {
        super(size, size, message, spriteSize, spriteSize, 0, 0, sprites, onPress, null, null, false);
    }

    @Override
    public boolean shouldTakeFocusAfterInteraction() {
        return false;
    }
}
