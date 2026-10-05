package net.kroet.tradeshuffler.neoforge;

import net.kroet.tradeshuffler.shared.TradeShufflerConstants;
import net.kroet.tradeshuffler.shared.server.ServerConfigHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

/**
 * Common (both sides) NeoForge entrypoint: server lifecycle wiring. Runs on
 * dedicated servers and on clients (for the integrated server).
 */
@Mod(TradeShufflerConstants.MOD_ID)
public final class TradeShufflerNeoForgeEntrypoint {
    public TradeShufflerNeoForgeEntrypoint(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
    }

    private void onServerStarting(ServerStartingEvent event) {
        ServerConfigHolder.load(FMLPaths.GAMEDIR.get());
    }
}
