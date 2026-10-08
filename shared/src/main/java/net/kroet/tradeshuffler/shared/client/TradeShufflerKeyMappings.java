package net.kroet.tradeshuffler.shared.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.kroet.tradeshuffler.shared.TradeShufflerConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * TradeShuffler's key mapping and its controls-screen category, shared by both
 * loaders. Each loader registers it through its own API (Fabric
 * {@code KeyMappingHelper}, NeoForge {@code RegisterKeyMappingsEvent}).
 */
public final class TradeShufflerKeyMappings {
    /**
     * Category label resolves to the
     * {@code key.category.tradeshuffler.tradeshuffler} lang key.
     */
    public static final Identifier CATEGORY_ID = Identifier.fromNamespaceAndPath(TradeShufflerConstants.MOD_ID, TradeShufflerConstants.MOD_ID);

    private static final String SHUFFLE_TRANSLATION_KEY = "key.tradeshuffler.shuffle";

    private static @Nullable KeyMapping shuffle;

    private TradeShufflerKeyMappings() {
    }

    /** Creates the shuffle key mapping, unbound by default, and remembers it. */
    public static KeyMapping createShuffle(KeyMapping.Category category) {
        shuffle = new KeyMapping(SHUFFLE_TRANSLATION_KEY, InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), category);
        return shuffle;
    }

    public static @Nullable KeyMapping shuffle() {
        return shuffle;
    }
}
