package net.kroet.tradeshuffler.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.kroet.tradeshuffler.common.config.TradeShufflerClientConfig;
import net.kroet.tradeshuffler.shared.client.ClientConfigHolder;
import net.kroet.tradeshuffler.shared.client.UpdateNoticeController;

/**
 * Client-only Fabric entrypoint: client config and update notice.
 */
public final class TradeShufflerFabricClientBootstrap implements ClientModInitializer {
    private static final UpdateNoticeController UPDATE_NOTICE = new UpdateNoticeController();

    @Override
    public void onInitializeClient() {
        TradeShufflerClientConfig config = ClientConfigHolder.load(FabricLoader.getInstance().getGameDir());
        UPDATE_NOTICE.scheduleUpdateCheck(config);
        ClientTickEvents.END_CLIENT_TICK.register(UPDATE_NOTICE::flushPendingUpdateMessage);
    }
}
