package net.kroet.tradeshuffler.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * Shared JSON read/write helpers for the config stores. Reading never throws: a
 * missing, unreadable or malformed file yields an empty result so callers fall
 * back to defaults; a malformed file is kept as {@code <name>.broken} first.
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
        } catch (JsonParseException e) {
            backUp(file);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Writes to a temporary file first, so a crash never leaves a half-written
     * config.
     */
    static void write(Path file, Object model) throws IOException {
        Path directory = file.toAbsolutePath().getParent();
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, file.getFileName().toString(), ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GSON.toJson(model, writer);
            }
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void backUp(Path file) {
        try {
            Files.move(file, file.resolveSibling(file.getFileName() + ".broken"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Leave the file in place; the next save overwrites it.
        }
    }
}
