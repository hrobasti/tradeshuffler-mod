package net.kroet.tradeshuffler.common.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * JSON-backed persistence for {@link TradeShufflerServerConfig}. Missing fields fall back to
 * their defaults, so new fields are additive and need no format version bump.
 */
public final class TradeShufflerServerConfigStore {
    static final int CURRENT_CONFIG_VERSION = 1;

    private final Path configFile;

    public TradeShufflerServerConfigStore(Path configFile) {
        this.configFile = Objects.requireNonNull(configFile, "configFile");
    }

    public TradeShufflerServerConfig load() {
        return JsonConfigFiles.read(configFile, FileModel.class)
            .map(FileModel::toConfig)
            .orElseGet(TradeShufflerServerConfig::defaults);
    }

    public void save(TradeShufflerServerConfig config) throws IOException {
        JsonConfigFiles.write(configFile, FileModel.fromConfig(config));
    }

    private record FileModel(
        Integer configVersion,
        Boolean shuffleEnabled,
        Boolean previewEnabled,
        Boolean requireJobSite,
        Integer shuffleCooldownTicks
    ) {
        static FileModel fromConfig(TradeShufflerServerConfig config) {
            return new FileModel(
                CURRENT_CONFIG_VERSION,
                config.shuffleEnabled(),
                config.previewEnabled(),
                config.requireJobSite(),
                config.shuffleCooldownTicks()
            );
        }

        TradeShufflerServerConfig toConfig() {
            TradeShufflerServerConfig defaults = TradeShufflerServerConfig.defaults();
            return new TradeShufflerServerConfig(
                shuffleEnabled == null ? defaults.shuffleEnabled() : shuffleEnabled,
                previewEnabled == null ? defaults.previewEnabled() : previewEnabled,
                requireJobSite == null ? defaults.requireJobSite() : requireJobSite,
                shuffleCooldownTicks == null ? defaults.shuffleCooldownTicks() : shuffleCooldownTicks
            );
        }
    }
}
