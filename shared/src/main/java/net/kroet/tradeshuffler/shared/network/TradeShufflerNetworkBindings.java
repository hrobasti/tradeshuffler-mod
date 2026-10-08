package net.kroet.tradeshuffler.shared.network;

import net.minecraft.server.level.ServerPlayer;

/**
 * Loader-specific networking behind two small interfaces, installed by the
 * loader entrypoints. Server and client parts are separate so a dedicated
 * server never loads client networking classes.
 */
public final class TradeShufflerNetworkBindings {
    /** Server side: sending to players. */
    public interface Server {
        /** Whether the player's client registered the TradeShuffler channels. */
        boolean canReceive(ServerPlayer player);

        void sendTradeState(ServerPlayer player, TradeStatePayload payload);
    }

    /** Client side: sending to the server. */
    public interface Client {
        /** Whether the connected server accepts shuffle requests. */
        boolean serverSupportsShuffle();

        void sendShuffleRequest(ShuffleRequestPayload payload);
    }

    private static final Server NO_SERVER = new Server() {
        @Override
        public boolean canReceive(ServerPlayer player) {
            return false;
        }

        @Override
        public void sendTradeState(ServerPlayer player, TradeStatePayload payload) {
        }
    };

    private static final Client NO_CLIENT = new Client() {
        @Override
        public boolean serverSupportsShuffle() {
            return false;
        }

        @Override
        public void sendShuffleRequest(ShuffleRequestPayload payload) {
        }
    };

    private static volatile Server server = NO_SERVER;
    private static volatile Client client = NO_CLIENT;

    private TradeShufflerNetworkBindings() {
    }

    public static void installServer(Server bindings) {
        server = bindings;
    }

    public static void installClient(Client bindings) {
        client = bindings;
    }

    public static Server server() {
        return server;
    }

    public static Client client() {
        return client;
    }
}
