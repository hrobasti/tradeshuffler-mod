package net.kroet.tradeshuffler.neoforge;

import net.kroet.tradeshuffler.shared.TradeShufflerConstants;
import net.kroet.tradeshuffler.shared.network.ShuffleRequestPayload;
import net.kroet.tradeshuffler.shared.network.TradeShufflerNetworkBindings;
import net.kroet.tradeshuffler.shared.network.TradeStatePayload;
import net.kroet.tradeshuffler.shared.server.ShuffleRequestHandler;
import net.kroet.tradeshuffler.shared.server.TradeShufflerServerLifecycle;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Common (both sides) NeoForge entrypoint: payload registration, server handler
 * and server lifecycle. Runs on dedicated servers and on clients (for the
 * integrated server).
 */
@Mod(TradeShufflerConstants.MOD_ID)
public final class TradeShufflerNeoForgeEntrypoint {
    private static final String PROTOCOL_VERSION = "1";

    public TradeShufflerNeoForgeEntrypoint(IEventBus modEventBus) {
        modEventBus.addListener(this::onRegisterPayloadHandlers);
        TradeShufflerNetworkBindings.installServer(new TradeShufflerNetworkBindings.Server() {
            @Override
            public boolean canReceive(ServerPlayer player) {
                return player.connection.hasChannel(TradeStatePayload.TYPE);
            }

            @Override
            public void sendTradeState(ServerPlayer player, TradeStatePayload payload) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        });
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> ShuffleRequestHandler.onPlayerLeft(event.getEntity().getUUID()));
    }

    private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        // Optional channels let vanilla clients join a TradeShuffler server and vice
        // versa; the features simply stay off there.
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION).optional();
        registrar.playToServer(ShuffleRequestPayload.TYPE, ShuffleRequestPayload.STREAM_CODEC,
                (payload, context) -> ShuffleRequestHandler.handle((ServerPlayer) context.player(), payload));
        // The client handler is registered in the client entrypoint.
        registrar.playToClient(TradeStatePayload.TYPE, TradeStatePayload.STREAM_CODEC);
    }

    private void onServerStarting(ServerStartingEvent event) {
        TradeShufflerServerLifecycle.onServerStarting(FMLPaths.GAMEDIR.get());
    }
}
