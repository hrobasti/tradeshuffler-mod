package net.kroet.tradeshuffler.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Shared JSON read/write helpers for the config stores. Reading never throws: a
 * missing, unreadable or malformed file yields an empty result so callers fall
 * back to defaults.
 */
final class JsonConfigFiles {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private JsonConfigFiles() {
    }

    static <M> Optional<M> read(Path file, Class<M> modelType) {
        if (!Files.exists(file)) {
            return Optional.empty();
        }

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return Optional.ofNullable(GSON.fromJson(reader, modelType));
        } catch (IOException | JsonParseException ignored) {
            return Optional.empty();
        }
    }

    static void write(Path file, Object model) throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }

        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(model, writer);
        }
    }
}
