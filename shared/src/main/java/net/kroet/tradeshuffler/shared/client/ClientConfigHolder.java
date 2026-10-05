package net.kroet.tradeshuffler.shared.client;

import java.io.IOException;
import java.nio.file.Path;
import net.kroet.tradeshuffler.common.config.TradeShufflerClientConfig;
import net.kroet.tradeshuffler.common.config.TradeShufflerClientConfigStore;
import net.kroet.tradeshuffler.shared.TradeShufflerConstants;

/**
 * Holds the active client config. Loaded once by the client entrypoint of each loader.
 */
public final class ClientConfigHolder {
    private static volatile TradeShufflerClientConfig current = TradeShufflerClientConfig.defaults();

    private ClientConfigHolder() {
    }

    /**
     * Loads the config from the game directory and writes it back, so a missing file is created
     * with defaults and out-of-range values are persisted in normalized form.
     */
    public static TradeShufflerClientConfig load(Path gameDirectory) {
        TradeShufflerClientConfigStore store = new TradeShufflerClientConfigStore(
            gameDirectory.resolve(TradeShufflerConstants.CLIENT_CONFIG_RELATIVE_PATH)
        );
        current = store.load();
        try {
            store.save(current);
        } catch (IOException ignored) {
            // Best-effort: the loaded (or default) values stay active even if the file is read-only.
        }
        return current;
    }

    public static TradeShufflerClientConfig current() {
        return current;
    }
}
