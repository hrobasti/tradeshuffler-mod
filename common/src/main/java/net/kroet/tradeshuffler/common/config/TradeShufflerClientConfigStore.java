package net.kroet.tradeshuffler.common.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * JSON-backed persistence for {@link TradeShufflerClientConfig}. Missing fields
 * fall back to their defaults, so new fields are additive and need no format
 * version bump.
 */
public final class TradeShufflerClientConfigStore {
    static final int CURRENT_CONFIG_VERSION = 1;

    private final Path configFile;

    public TradeShufflerClientConfigStore(Path configFile) {
        this.configFile = Objects.requireNonNull(configFile, "configFile");
    }

    public TradeShufflerClientConfig load() {
        return JsonConfigFiles.read(configFile, FileModel.class)
                .map(FileModel::toConfig)
                .orElseGet(TradeShufflerClientConfig::defaults);
    }

    public void save(TradeShufflerClientConfig config) throws IOException {
        JsonConfigFiles.write(configFile, FileModel.fromConfig(config));
    }

    private record FileModel(
            Integer configVersion,
            Boolean shuffleButtonEnabled,
            Integer shuffleButtonOffsetX,
            Integer shuffleButtonOffsetY,
            Boolean previewEnabled,
            Integer previewDimAlpha,
            Boolean previewLevelStripe,
            Boolean updateCheckEnabled,
            String updateChannel) {
        static FileModel fromConfig(TradeShufflerClientConfig config) {
            return new FileModel(
                    CURRENT_CONFIG_VERSION,
                    config.shuffleButtonEnabled(),
                    config.shuffleButtonOffsetX(),
                    config.shuffleButtonOffsetY(),
                    config.previewEnabled(),
                    config.previewDimAlpha(),
                    config.previewLevelStripe(),
                    config.updateCheckEnabled(),
                    config.updateChannel().name());
        }

        TradeShufflerClientConfig toConfig() {
            TradeShufflerClientConfig defaults = TradeShufflerClientConfig.defaults();
            return new TradeShufflerClientConfig(
                    shuffleButtonEnabled == null ? defaults.shuffleButtonEnabled() : shuffleButtonEnabled,
                    shuffleButtonOffsetX == null ? defaults.shuffleButtonOffsetX() : shuffleButtonOffsetX,
                    shuffleButtonOffsetY == null ? defaults.shuffleButtonOffsetY() : shuffleButtonOffsetY,
                    previewEnabled == null ? defaults.previewEnabled() : previewEnabled,
                    previewDimAlpha == null ? defaults.previewDimAlpha() : previewDimAlpha,
                    previewLevelStripe == null ? defaults.previewLevelStripe() : previewLevelStripe,
                    updateCheckEnabled == null ? defaults.updateCheckEnabled() : updateCheckEnabled,
                    UpdateChannel.fromString(updateChannel, defaults.updateChannel()));
        }
    }
}
