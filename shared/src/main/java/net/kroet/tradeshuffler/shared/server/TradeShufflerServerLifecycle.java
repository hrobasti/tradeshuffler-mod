package net.kroet.tradeshuffler.shared.server;

import java.nio.file.Path;

/**
 * Server lifecycle hooks shared by both loaders (dedicated and integrated
 * server).
 */
public final class TradeShufflerServerLifecycle {
    private TradeShufflerServerLifecycle() {
    }

    public static void onServerStarting(Path serverDirectory) {
        ServerConfigHolder.load(serverDirectory);
        ShuffleRequestHandler.reset();
        TradePlanService.reset();
    }
}
