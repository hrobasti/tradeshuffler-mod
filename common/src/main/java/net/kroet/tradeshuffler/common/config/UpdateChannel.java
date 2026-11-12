package net.kroet.tradeshuffler.common.config;

import java.util.Locale;
import org.jspecify.annotations.Nullable;

/**
 * User-facing update release channels for TradeShuffler.
 */
public enum UpdateChannel {
    STABLE, BETA, ALPHA;

    public static UpdateChannel fromString(@Nullable String raw, UpdateChannel fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return UpdateChannel.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}
