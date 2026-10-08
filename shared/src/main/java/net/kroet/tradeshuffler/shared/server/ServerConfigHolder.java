package net.kroet.tradeshuffler.shared.server;

import java.io.IOException;
import java.nio.file.Path;
import net.kroet.tradeshuffler.common.config.TradeShufflerServerConfig;
import net.kroet.tradeshuffler.common.config.TradeShufflerServerConfigStore;
import net.kroet.tradeshuffler.shared.TradeShufflerConstants;

/**
 * Holds the active server config. Loaded by the loader entrypoints when a
 * server (dedicated or integrated) starts; until then the defaults apply.
 */
public final class ServerConfigHolder {
    private static volatile TradeShufflerServerConfig current = TradeShufflerServerConfig.defaults();

    private ServerConfigHolder() {
    }

    /**
     * Loads the config from the server directory and writes it back, so a missing
     * file is created with defaults and out-of-range values are persisted in
     * normalized form.
     */
    public static TradeShufflerServerConfig load(Path serverDirectory) {
        TradeShufflerServerConfigStore store = new TradeShufflerServerConfigStore(
                serverDirectory.resolve(TradeShufflerConstants.SERVER_CONFIG_RELATIVE_PATH));
        current = store.load();
        try {
            store.save(current);
        } catch (IOException ignored) {
            // The loaded (or default) values stay active if the file is read-only.
        }
        return current;
    }

    public static TradeShufflerServerConfig current() {
        return current;
    }
}
