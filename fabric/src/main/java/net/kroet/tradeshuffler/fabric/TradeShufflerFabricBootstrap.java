package net.kroet.tradeshuffler.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.kroet.tradeshuffler.shared.network.ShuffleRequestPayload;
import net.kroet.tradeshuffler.shared.network.TradeShufflerNetworkBindings;
import net.kroet.tradeshuffler.shared.network.TradeStatePayload;
import net.kroet.tradeshuffler.shared.server.ShuffleRequestHandler;
import net.kroet.tradeshuffler.shared.server.TradeShufflerServerLifecycle;
import net.minecraft.server.level.ServerPlayer;

/**
 * Common (both sides) Fabric entrypoint: payload registration, server receiver
 * and server lifecycle. Runs on dedicated servers and on clients (for the
 * integrated server).
 */
public final class TradeShufflerFabricBootstrap implements ModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(ShuffleRequestPayload.TYPE, ShuffleRequestPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TradeStatePayload.TYPE, TradeStatePayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ShuffleRequestPayload.TYPE, (payload, context) -> ShuffleRequestHandler.handle(context.player(), payload));
        TradeShufflerNetworkBindings.installServer(new TradeShufflerNetworkBindings.Server() {
            @Override
            public boolean canReceive(ServerPlayer player) {
                return ServerPlayNetworking.canSend(player, TradeStatePayload.TYPE);
            }

            @Override
            public void sendTradeState(ServerPlayer player, TradeStatePayload payload) {
                ServerPlayNetworking.send(player, payload);
            }
        });

        ServerLifecycleEvents.SERVER_STARTING.register(server -> TradeShufflerServerLifecycle.onServerStarting(FabricLoader.getInstance().getGameDir()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> ShuffleRequestHandler.onPlayerLeft(handler.getPlayer().getUUID()));
    }
}
