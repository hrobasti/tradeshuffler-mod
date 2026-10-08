package net.kroet.tradeshuffler.common.rules;

/**
 * Server-side rules that decide whether a villager's trades may be shuffled.
 * The Minecraft layer only collects the facts; this class decides, so every
 * rule is testable without Minecraft. Rules are checked in a fixed order and
 * the first failing one is reported.
 */
public final class ShuffleEligibility {
    /** Level of a villager that has never levelled up. */
    public static final int NOVICE_LEVEL = 1;

    /** Outcome of a check; everything except {@link #ALLOWED} rejects. */
    public enum Verdict {
        ALLOWED, SHUFFLE_DISABLED, STALE_CONTAINER, NOT_A_VILLAGER, NOT_TRADING_WITH_PLAYER, ALREADY_TRADED, HAS_XP, NOT_NOVICE, NO_TRADES, NO_JOB_SITE, COOLDOWN;

        public boolean allowed() {
            return this == ALLOWED;
        }
    }

    /**
     * Facts about one shuffle attempt.
     *
     * @param shuffleEnabled
     *            server config: shuffling is enabled
     * @param containerMatches
     *            the player's open menu is a merchant menu with the requested
     *            container id
     * @param villager
     *            the menu's trader is a villager (not a wandering trader or another
     *            merchant)
     * @param tradingWithPlayer
     *            the villager is alive and trading with this player
     * @param traded
     *            the persistent traded flag is set
     * @param villagerXp
     *            the villager's trade XP
     * @param villagerLevel
     *            the villager's profession level
     * @param hasNoviceTrades
     *            the profession has a trade set for the novice level
     * @param requireJobSite
     *            server config: a job site is required
     * @param hasJobSite
     *            the villager remembers a job site
     * @param cooldownElapsed
     *            the player's shuffle cooldown has elapsed
     */
    public record Input(
            boolean shuffleEnabled,
            boolean containerMatches,
            boolean villager,
            boolean tradingWithPlayer,
            boolean traded,
            int villagerXp,
            int villagerLevel,
            boolean hasNoviceTrades,
            boolean requireJobSite,
            boolean hasJobSite,
            boolean cooldownElapsed) {
    }

    private ShuffleEligibility() {
    }

    public static Verdict evaluate(Input input) {
        if (!input.shuffleEnabled()) {
            return Verdict.SHUFFLE_DISABLED;
        }
        if (!input.containerMatches()) {
            return Verdict.STALE_CONTAINER;
        }
        if (!input.villager()) {
            return Verdict.NOT_A_VILLAGER;
        }
        if (!input.tradingWithPlayer()) {
            return Verdict.NOT_TRADING_WITH_PLAYER;
        }
        if (input.traded()) {
            return Verdict.ALREADY_TRADED;
        }
        // XP and level cover villagers traded with before the mod was installed and
        // villagers whose level was changed by commands.
        if (input.villagerXp() != 0) {
            return Verdict.HAS_XP;
        }
        if (input.villagerLevel() != NOVICE_LEVEL) {
            return Verdict.NOT_NOVICE;
        }
        if (!input.hasNoviceTrades()) {
            return Verdict.NO_TRADES;
        }
        if (input.requireJobSite() && !input.hasJobSite()) {
            return Verdict.NO_JOB_SITE;
        }
        if (!input.cooldownElapsed()) {
            return Verdict.COOLDOWN;
        }
        return Verdict.ALLOWED;
    }
}
