package net.kroet.tradeshuffler.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TradeShufflerServerConfigStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void loadReturnsDefaultsWhenFileMissing() {
        TradeShufflerServerConfigStore store = new TradeShufflerServerConfigStore(tempDir.resolve("tradeshuffler-server.json"));

        assertEquals(TradeShufflerServerConfig.defaults(), store.load());
    }

    @Test
    void saveAndLoadRoundTrip() throws IOException {
        TradeShufflerServerConfigStore store = new TradeShufflerServerConfigStore(tempDir.resolve("config/tradeshuffler-server.json"));
        TradeShufflerServerConfig original = new TradeShufflerServerConfig(false, false, false, 40);

        store.save(original);

        assertEquals(original, store.load());
    }

    @Test
    void loadFallsBackOnInvalidJson() throws IOException {
        Path file = tempDir.resolve("tradeshuffler-server.json");
        Files.writeString(file, "[1, 2");

        assertEquals(TradeShufflerServerConfig.defaults(), new TradeShufflerServerConfigStore(file).load());
    }

    @Test
    void loadClampsCooldownAndKeepsDefaultsForMissingFields() throws IOException {
        Path file = tempDir.resolve("tradeshuffler-server.json");
        Files.writeString(file, "{ \"shuffleCooldownTicks\": 9999, \"requireJobSite\": false }");

        TradeShufflerServerConfig config = new TradeShufflerServerConfigStore(file).load();

        assertEquals(TradeShufflerServerConfig.MAX_SHUFFLE_COOLDOWN_TICKS, config.shuffleCooldownTicks());
        assertEquals(false, config.requireJobSite());
        assertEquals(TradeShufflerServerConfig.defaults().shuffleEnabled(), config.shuffleEnabled());
        assertEquals(TradeShufflerServerConfig.defaults().previewEnabled(), config.previewEnabled());
    }
}
