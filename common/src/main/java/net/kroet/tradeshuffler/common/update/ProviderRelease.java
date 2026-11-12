package net.kroet.tradeshuffler.common.update;

import org.jspecify.annotations.Nullable;

/**
 * Newest release found on one platform; {@code url} may be null.
 */
public record ProviderRelease(String version, @Nullable String url) {
}
