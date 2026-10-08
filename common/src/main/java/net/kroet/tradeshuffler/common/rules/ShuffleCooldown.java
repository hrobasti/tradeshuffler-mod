package net.kroet.tradeshuffler.common.rules;

import java.util.HashMap;
import java.util.Map;

/**
 * Minimum time between two accepted shuffles of the same player, measured in
 * server ticks. Not thread-safe: used from the server thread only.
 *
 * @param <K>
 *            player key, e.g. the player's UUID
 */
public final class ShuffleCooldown<K> {
    private final Map<K, Long> lastUsedTick = new HashMap<>();

    public boolean isElapsed(K key, long currentTick, int cooldownTicks) {
        Long lastUsed = lastUsedTick.get(key);
        // A tick counter that went backwards (new server instance) never blocks.
        return lastUsed == null || currentTick < lastUsed || currentTick - lastUsed >= cooldownTicks;
    }

    public void markUsed(K key, long currentTick) {
        lastUsedTick.put(key, currentTick);
    }

    public void forget(K key) {
        lastUsedTick.remove(key);
    }

    public void clear() {
        lastUsedTick.clear();
    }
}
