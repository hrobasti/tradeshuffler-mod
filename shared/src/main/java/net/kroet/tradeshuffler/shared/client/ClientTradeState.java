package net.kroet.tradeshuffler.shared.client;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import net.kroet.tradeshuffler.shared.network.TradeStatePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.MerchantMenu;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Latest trade state the server sent for the open merchant menu. Client thread
 * only; payload handlers of both loaders run on that thread.
 */
public final class ClientTradeState {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static @Nullable TradeStatePayload current;
    private static long revision;

    private ClientTradeState() {
    }

    public static void accept(Minecraft minecraft, TradeStatePayload payload) {
        LocalPlayer player = minecraft.player;
        if (player == null || !(player.containerMenu instanceof MerchantMenu menu) || menu.containerId != payload.containerId()) {
            LOGGER.debug("Ignored trade state for container {}: no matching merchant menu open", payload.containerId());
            return;
        }
        current = payload;
        revision++;
        LOGGER.debug("Trade state for container {}: block reason '{}', preview batches={}",
                payload.containerId(), payload.shuffleBlockReason(), payload.preview().size());
    }

    /** State for the given container, empty if the server sent none for it. */
    public static Optional<TradeStatePayload> forContainer(int containerId) {
        TradeStatePayload state = current;
        return state != null && state.containerId() == containerId ? Optional.of(state) : Optional.empty();
    }

    /**
     * Increases with every accepted state, so callers can wait for the next one.
     */
    public static long revision() {
        return revision;
    }

    /**
     * Called when a merchant screen opens and on disconnect, so an old state never
     * matches a later menu that reuses the container id.
     */
    public static void clear() {
        current = null;
    }
}
