package net.kroet.tradeshuffler.shared.client;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CompletableFuture;
import net.kroet.tradeshuffler.common.config.TradeShufflerClientConfig;
import net.kroet.tradeshuffler.common.core.TradeShufflerTextKeys;
import net.kroet.tradeshuffler.common.update.ModLoader;
import net.kroet.tradeshuffler.common.update.TradeShufflerUpdateService;
import net.kroet.tradeshuffler.common.update.UpdateProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

/**
 * Background update check whose result is flushed to chat once the player is
 * available. Shared between Fabric and NeoForge; each loader's client
 * entrypoint owns one instance.
 */
public final class UpdateNoticeController {
    private static final Logger LOGGER = LogUtils.getLogger();
    private volatile TradeShufflerUpdateService.UpdateStatus pendingUpdateStatus;

    /**
     * @param currentVersion
     *            the mod version as reported by the loader; the check is skipped
     *            when it is unknown
     */
    public void scheduleUpdateCheck(TradeShufflerClientConfig config, ModLoader loader, String currentVersion) {
        if (config == null || !config.updateCheckEnabled()) {
            return;
        }

        if (currentVersion == null || currentVersion.isBlank()) {
            LOGGER.info("Update check skipped: mod version unknown");
            return;
        }
        String minecraftVersion = SharedConstants.getCurrentVersion().name();
        var updateChannel = config.updateChannel();

        CompletableFuture.runAsync(() -> {
            TradeShufflerUpdateService.UpdateStatus status = TradeShufflerUpdateService.checkForUpdates(
                    currentVersion,
                    minecraftVersion,
                    updateChannel,
                    loader);
            status.errors().forEach((provider, reason) -> LOGGER.info("Update check: {} unavailable ({})", provider.label(), reason));
            if (status.hasUpdate()) {
                pendingUpdateStatus = status;
            }
        }).exceptionally(error -> {
            LOGGER.warn("Update check failed", error);
            return null;
        });
    }

    public void flushPendingUpdateMessage(Minecraft minecraft) {
        TradeShufflerUpdateService.UpdateStatus status = pendingUpdateStatus;
        if (status == null || !status.hasUpdate()) {
            return;
        }
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        LocalPlayer player = minecraft.player;

        ClientMessages.sendChat(player, Component.translatable(TradeShufflerTextKeys.UPDATE_AVAILABLE, status.currentVersion(), status.latestVersion())
                .withStyle(ChatFormatting.WHITE));
        for (UpdateProvider provider : UpdateProvider.values()) {
            ClientMessages.displayProviderLine(player, status, provider);
        }

        pendingUpdateStatus = null;
    }
}
