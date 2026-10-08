package net.kroet.tradeshuffler.neoforge;

import net.kroet.tradeshuffler.common.config.TradeShufflerClientConfig;
import net.kroet.tradeshuffler.common.update.ModLoader;
import net.kroet.tradeshuffler.shared.TradeShufflerConstants;
import net.kroet.tradeshuffler.shared.client.ClientConfigHolder;
import net.kroet.tradeshuffler.shared.client.ClientTradeState;
import net.kroet.tradeshuffler.shared.client.TradeShufflerConfigScreen;
import net.kroet.tradeshuffler.shared.client.TradeShufflerKeyMappings;
import net.kroet.tradeshuffler.shared.client.UpdateNoticeController;
import net.kroet.tradeshuffler.shared.network.ShuffleRequestPayload;
import net.kroet.tradeshuffler.shared.network.TradeShufflerNetworkBindings;
import net.kroet.tradeshuffler.shared.network.TradeStatePayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client-only NeoForge entrypoint (constructed only on the physical client):
 * client config and config screen, key mapping, trade-state handler and update
 * notice.
 */
@Mod(value = TradeShufflerConstants.MOD_ID, dist = Dist.CLIENT)
public final class TradeShufflerNeoForgeClientEntrypoint {
    private final UpdateNoticeController updateNotice = new UpdateNoticeController();

    public TradeShufflerNeoForgeClientEntrypoint(IEventBus modEventBus, ModContainer container) {
        TradeShufflerClientConfig config = ClientConfigHolder.load(FMLPaths.GAMEDIR.get());
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, parent) -> new TradeShufflerConfigScreen(parent));

        modEventBus.addListener(this::onRegisterKeyMappings);
        modEventBus.addListener(this::onRegisterClientPayloadHandlers);
        NeoForge.EVENT_BUS.addListener(this::onLoggingOut);
        TradeShufflerNetworkBindings.installClient(new TradeShufflerNetworkBindings.Client() {
            @Override
            public boolean serverSupportsShuffle() {
                ClientPacketListener connection = Minecraft.getInstance().getConnection();
                return connection != null && connection.hasChannel(ShuffleRequestPayload.TYPE);
            }

            @Override
            public void sendShuffleRequest(ShuffleRequestPayload payload) {
                ClientPacketDistributor.sendToServer(payload);
            }
        });

        updateNotice.scheduleUpdateCheck(config, ModLoader.NEOFORGE, container.getModInfo().getVersion().toString());
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        KeyMapping.Category category = new KeyMapping.Category(TradeShufflerKeyMappings.CATEGORY_ID);
        event.registerCategory(category);
        event.register(TradeShufflerKeyMappings.createShuffle(category));
    }

    private void onRegisterClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(TradeStatePayload.TYPE, (payload, context) -> ClientTradeState.accept(Minecraft.getInstance(), payload));
    }

    private void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientTradeState.clear();
    }

    private void onClientTick(ClientTickEvent.Post event) {
        updateNotice.flushPendingUpdateMessage(Minecraft.getInstance());
    }
}
