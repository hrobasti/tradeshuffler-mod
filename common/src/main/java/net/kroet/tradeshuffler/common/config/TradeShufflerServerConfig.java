package net.kroet.tradeshuffler.common.config;

/**
 * Loader-neutral server config model
 * ({@code config/tradeshuffler-server.json}). File-only, loaded on server start
 * (dedicated and integrated server). Values are normalized on construction.
 *
 * @param shuffleEnabled
 *            whether players may shuffle villager trades
 * @param previewEnabled
 *            whether future-level trades are generated and sent to clients
 * @param requireJobSite
 *            whether a villager needs a job site memory before it can be
 *            shuffled
 * @param shuffleCooldownTicks
 *            minimum ticks between two shuffle requests of the same player
 */
public record TradeShufflerServerConfig(
        boolean shuffleEnabled,
        boolean previewEnabled,
        boolean requireJobSite,
        int shuffleCooldownTicks) {
    public static final int MIN_SHUFFLE_COOLDOWN_TICKS = 0;
    public static final int MAX_SHUFFLE_COOLDOWN_TICKS = 200;
    public static final int DEFAULT_SHUFFLE_COOLDOWN_TICKS = 10;

    public TradeShufflerServerConfig {
        shuffleCooldownTicks = clampShuffleCooldownTicks(shuffleCooldownTicks);
    }

    public static TradeShufflerServerConfig defaults() {
        return new TradeShufflerServerConfig(true, true, true, DEFAULT_SHUFFLE_COOLDOWN_TICKS);
    }

    public static int clampShuffleCooldownTicks(int ticks) {
        return Math.max(MIN_SHUFFLE_COOLDOWN_TICKS, Math.min(MAX_SHUFFLE_COOLDOWN_TICKS, ticks));
    }
}
