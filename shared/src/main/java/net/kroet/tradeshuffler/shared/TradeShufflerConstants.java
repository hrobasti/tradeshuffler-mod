package net.kroet.tradeshuffler.shared;

/**
 * Identifiers and file locations shared by both loaders.
 */
public final class TradeShufflerConstants {
    public static final String MOD_ID = "tradeshuffler";

    /** Client config file, relative to the game directory. */
    public static final String CLIENT_CONFIG_RELATIVE_PATH = "config/tradeshuffler.json";

    /** Server config file, relative to the game (server) directory. */
    public static final String SERVER_CONFIG_RELATIVE_PATH = "config/tradeshuffler-server.json";

    private TradeShufflerConstants() {
    }
}
