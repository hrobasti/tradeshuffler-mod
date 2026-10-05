package net.kroet.tradeshuffler.common.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * All bundled locale files must be structurally identical to the en_us fallback: same keys in
 * the same order, and no blank values.
 */
class LocaleFilesConsistencyTest {
    private static final Path LANG_DIR = Path.of("src/main/resources/assets/tradeshuffler/lang");
    private static final int EXPECTED_LOCALE_COUNT = 14;

    @Test
    void allLocalesHaveTheSameKeysInTheSameOrder() throws IOException {
        List<String> reference = keys(LANG_DIR.resolve("en_us.json"));
        List<Path> files = localeFiles();

        assertEquals(EXPECTED_LOCALE_COUNT, files.size(), "number of bundled locale files");
        for (Path file : files) {
            assertEquals(reference, keys(file), "keys/order of " + file.getFileName());
        }
    }

    @Test
    void noLocaleHasBlankValues() throws IOException {
        for (Path file : localeFiles()) {
            for (Map.Entry<String, JsonElement> entry : read(file).entrySet()) {
                assertFalse(entry.getValue().getAsString().isBlank(), file.getFileName() + ": " + entry.getKey());
            }
        }
    }

    private static List<Path> localeFiles() throws IOException {
        try (Stream<Path> stream = Files.list(LANG_DIR)) {
            return stream.filter(path -> path.toString().endsWith(".json")).sorted().toList();
        }
    }

    private static List<String> keys(Path file) throws IOException {
        return new ArrayList<>(read(file).keySet());
    }

    private static JsonObject read(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
