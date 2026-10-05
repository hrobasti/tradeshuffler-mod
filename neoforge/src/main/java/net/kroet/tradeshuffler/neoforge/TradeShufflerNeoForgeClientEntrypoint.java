package net.kroet.tradeshuffler.neoforge;

import net.kroet.tradeshuffler.common.config.TradeShufflerClientConfig;
import net.kroet.tradeshuffler.shared.TradeShufflerConstants;
import net.kroet.tradeshuffler.shared.client.ClientConfigHolder;
import net.kroet.tradeshuffler.shared.client.UpdateNoticeController;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client-only NeoForge entrypoint (constructed only on the physical client): client config and
 * update notice.
 */
@Mod(value = TradeShufflerConstants.MOD_ID, dist = Dist.CLIENT)
public final class TradeShufflerNeoForgeClientEntrypoint {
    private final UpdateNoticeController updateNotice = new UpdateNoticeController();

    public TradeShufflerNeoForgeClientEntrypoint(IEventBus modEventBus) {
        TradeShufflerClientConfig config = ClientConfigHolder.load(FMLPaths.GAMEDIR.get());
        updateNotice.scheduleUpdateCheck(config);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        updateNotice.flushPendingUpdateMessage(Minecraft.getInstance());
    }
}
