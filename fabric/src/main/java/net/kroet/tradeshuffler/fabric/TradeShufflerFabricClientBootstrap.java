package net.kroet.tradeshuffler.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.kroet.tradeshuffler.common.config.TradeShufflerClientConfig;
import net.kroet.tradeshuffler.common.update.ModLoader;
import net.kroet.tradeshuffler.shared.TradeShufflerConstants;
import net.kroet.tradeshuffler.shared.client.ClientConfigHolder;
import net.kroet.tradeshuffler.shared.client.ClientTradeState;
import net.kroet.tradeshuffler.shared.client.TradeShufflerKeyMappings;
import net.kroet.tradeshuffler.shared.client.UpdateNoticeController;
import net.kroet.tradeshuffler.shared.network.ShuffleRequestPayload;
import net.kroet.tradeshuffler.shared.network.TradeShufflerNetworkBindings;
import net.kroet.tradeshuffler.shared.network.TradeStatePayload;
import net.minecraft.client.KeyMapping;

/**
 * Client-only Fabric entrypoint: client config, key mapping, trade-state
 * receiver and update notice.
 */
public final class TradeShufflerFabricClientBootstrap implements ClientModInitializer {
    private static final UpdateNoticeController UPDATE_NOTICE = new UpdateNoticeController();

    @Override
    public void onInitializeClient() {
        TradeShufflerClientConfig config = ClientConfigHolder.load(FabricLoader.getInstance().getGameDir());

        KeyMapping.Category category = KeyMapping.Category.register(TradeShufflerKeyMappings.CATEGORY_ID);
        KeyMappingHelper.registerKeyMapping(TradeShufflerKeyMappings.createShuffle(category));

        ClientPlayNetworking.registerGlobalReceiver(TradeStatePayload.TYPE, (payload, context) -> ClientTradeState.accept(context.client(), payload));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientTradeState.clear());
        TradeShufflerNetworkBindings.installClient(new TradeShufflerNetworkBindings.Client() {
            @Override
            public boolean serverSupportsShuffle() {
                return ClientPlayNetworking.canSend(ShuffleRequestPayload.TYPE);
            }

            @Override
            public void sendShuffleRequest(ShuffleRequestPayload payload) {
                ClientPlayNetworking.send(payload);
            }
        });

        UPDATE_NOTICE.scheduleUpdateCheck(
                config,
                ModLoader.FABRIC,
                FabricLoader.getInstance()
                        .getModContainer(TradeShufflerConstants.MOD_ID)
                        .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                        .orElse(null));
        ClientTickEvents.END_CLIENT_TICK.register(UPDATE_NOTICE::flushPendingUpdateMessage);
    }
}
