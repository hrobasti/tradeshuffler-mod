package net.kroet.tradeshuffler.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.kroet.tradeshuffler.shared.server.ServerConfigHolder;

/**
 * Common (both sides) Fabric entrypoint: server lifecycle wiring. Runs on
 * dedicated servers and on clients (for the integrated server).
 */
public final class TradeShufflerFabricBootstrap implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> ServerConfigHolder.load(FabricLoader.getInstance().getGameDir()));
    }
}
