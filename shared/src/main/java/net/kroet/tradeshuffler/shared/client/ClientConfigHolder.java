package net.kroet.tradeshuffler.shared.client;

import java.io.IOException;
import java.nio.file.Path;
import net.kroet.tradeshuffler.common.config.TradeShufflerClientConfig;
import net.kroet.tradeshuffler.common.config.TradeShufflerClientConfigStore;
import net.kroet.tradeshuffler.shared.TradeShufflerConstants;
import org.jspecify.annotations.Nullable;

/**
 * Holds the active client config. Loaded once by the client entrypoint of each
 * loader, saved by the config screen.
 */
public final class ClientConfigHolder {
    private static volatile TradeShufflerClientConfig current = TradeShufflerClientConfig.defaults();
    private static volatile @Nullable TradeShufflerClientConfigStore store;

    private ClientConfigHolder() {
    }

    /**
     * Loads the config from the game directory and writes it back, so a missing
     * file is created with defaults and out-of-range values are persisted in
     * normalized form.
     */
    public static TradeShufflerClientConfig load(Path gameDirectory) {
        TradeShufflerClientConfigStore loadedStore = new TradeShufflerClientConfigStore(
                gameDirectory.resolve(TradeShufflerConstants.CLIENT_CONFIG_RELATIVE_PATH));
        store = loadedStore;
        current = loadedStore.load();
        save(current);
        return current;
    }

    /** Applies the config right away and writes it to the config file. */
    public static void save(TradeShufflerClientConfig config) {
        current = config;
        TradeShufflerClientConfigStore activeStore = store;
        if (activeStore == null) {
            return;
        }
        try {
            activeStore.save(config);
        } catch (IOException ignored) {
            // Best-effort: the values stay active even if the file is read-only.
        }
    }

    public static TradeShufflerClientConfig current() {
        return current;
    }
}
