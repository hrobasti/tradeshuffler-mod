package net.kroet.tradeshuffler.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TradeShufflerClientConfigStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void loadReturnsDefaultsWhenFileMissing() {
        TradeShufflerClientConfigStore store = new TradeShufflerClientConfigStore(tempDir.resolve("tradeshuffler.json"));

        assertEquals(TradeShufflerClientConfig.defaults(), store.load());
    }

    @Test
    void saveAndLoadRoundTrip() throws IOException {
        TradeShufflerClientConfigStore store = new TradeShufflerClientConfigStore(tempDir.resolve("config/tradeshuffler.json"));
        TradeShufflerClientConfig original = new TradeShufflerClientConfig(false, -12, 30, false, 80, false, false, UpdateChannel.ALPHA);

        store.save(original);

        assertEquals(original, store.load());
    }

    @Test
    void loadFallsBackOnInvalidJson() throws IOException {
        Path file = tempDir.resolve("tradeshuffler.json");
        Files.writeString(file, "{ not-valid-json ");

        assertEquals(TradeShufflerClientConfig.defaults(), new TradeShufflerClientConfigStore(file).load());
        assertEquals("{ not-valid-json ", Files.readString(tempDir.resolve("tradeshuffler.json.broken")));
    }

    @Test
    void saveReplacesTheFileWithoutLeftovers() throws IOException {
        Path file = tempDir.resolve("tradeshuffler.json");
        TradeShufflerClientConfigStore store = new TradeShufflerClientConfigStore(file);

        store.save(TradeShufflerClientConfig.defaults());
        store.save(TradeShufflerClientConfig.defaults());

        assertEquals(TradeShufflerClientConfig.defaults(), store.load());
        try (var files = Files.list(tempDir)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void loadNormalizesOutOfRangeAndUnknownValues() throws IOException {
        Path file = tempDir.resolve("tradeshuffler.json");
        Files.writeString(file, """
                {
                  "shuffleButtonOffsetX": -500,
                  "shuffleButtonOffsetY": 500,
                  "previewDimAlpha": 250,
                  "updateChannel": "nightly"
                }
                """);

        TradeShufflerClientConfig config = new TradeShufflerClientConfigStore(file).load();

        assertEquals(TradeShufflerClientConfig.MIN_BUTTON_OFFSET, config.shuffleButtonOffsetX());
        assertEquals(TradeShufflerClientConfig.MAX_BUTTON_OFFSET, config.shuffleButtonOffsetY());
        assertEquals(TradeShufflerClientConfig.MAX_PREVIEW_DIM_ALPHA, config.previewDimAlpha());
        assertEquals(UpdateChannel.BETA, config.updateChannel());
    }

    @Test
    void missingFieldsUseDefaults() throws IOException {
        Path file = tempDir.resolve("tradeshuffler.json");
        Files.writeString(file, "{ \"previewEnabled\": false }");

        TradeShufflerClientConfig config = new TradeShufflerClientConfigStore(file).load();
        TradeShufflerClientConfig defaults = TradeShufflerClientConfig.defaults();

        assertEquals(false, config.previewEnabled());
        assertEquals(defaults.shuffleButtonEnabled(), config.shuffleButtonEnabled());
        assertEquals(defaults.previewDimAlpha(), config.previewDimAlpha());
        assertEquals(defaults.updateChannel(), config.updateChannel());
    }
}
