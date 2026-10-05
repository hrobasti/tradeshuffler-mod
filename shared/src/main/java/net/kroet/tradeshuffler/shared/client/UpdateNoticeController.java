package net.kroet.tradeshuffler.shared.client;

import java.util.concurrent.CompletableFuture;
import net.kroet.tradeshuffler.common.config.TradeShufflerClientConfig;
import net.kroet.tradeshuffler.common.core.TradeShufflerTextKeys;
import net.kroet.tradeshuffler.common.update.TradeShufflerUpdateService;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/**
 * Background update check whose result is flushed to chat once the player is
 * available. Shared between Fabric and NeoForge; each loader's client
 * entrypoint owns one instance.
 */
public final class UpdateNoticeController {
    private volatile TradeShufflerUpdateService.UpdateStatus pendingUpdateStatus;

    public void scheduleUpdateCheck(TradeShufflerClientConfig config) {
        if (config == null || !config.updateCheckEnabled()) {
            return;
        }

        String currentVersion = ClientMessages.resolveCurrentVersion();
        String minecraftVersion = SharedConstants.getCurrentVersion().toString();
        var updateChannel = config.updateChannel();

        CompletableFuture.runAsync(() -> {
            TradeShufflerUpdateService.UpdateStatus status = TradeShufflerUpdateService.checkForUpdates(
                    currentVersion,
                    minecraftVersion,
                    updateChannel);

            if (status.hasUpdate()) {
                pendingUpdateStatus = status;
            }
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

        ClientMessages.displayClientMessageCompat(
                player,
                Component.translatable(TradeShufflerTextKeys.UPDATE_AVAILABLE, status.currentVersion(), status.latestVersion())
                        .withStyle(ChatFormatting.WHITE),
                false);
        ClientMessages.displayProviderLine(player, status, "modrinth", "Modrinth");
        ClientMessages.displayProviderLine(player, status, "curseforge", "CurseForge");

        pendingUpdateStatus = null;
    }
}
