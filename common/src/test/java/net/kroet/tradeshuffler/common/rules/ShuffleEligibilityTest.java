package net.kroet.tradeshuffler.common.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kroet.tradeshuffler.common.rules.ShuffleEligibility.Input;
import net.kroet.tradeshuffler.common.rules.ShuffleEligibility.Verdict;
import org.junit.jupiter.api.Test;

class ShuffleEligibilityTest {
    private static final class Facts {
        boolean shuffleEnabled = true;
        boolean containerMatches = true;
        boolean villager = true;
        boolean tradingWithPlayer = true;
        boolean traded = false;
        int villagerXp = 0;
        int villagerLevel = ShuffleEligibility.NOVICE_LEVEL;
        boolean hasNoviceTrades = true;
        boolean requireJobSite = true;
        boolean hasJobSite = true;
        boolean cooldownElapsed = true;

        Verdict evaluate() {
            return ShuffleEligibility.evaluate(new Input(
                    shuffleEnabled,
                    containerMatches,
                    villager,
                    tradingWithPlayer,
                    traded,
                    villagerXp,
                    villagerLevel,
                    hasNoviceTrades,
                    requireJobSite,
                    hasJobSite,
                    cooldownElapsed));
        }
    }

    @Test
    void freshNoviceWithJobSiteIsAllowed() {
        Verdict verdict = new Facts().evaluate();

        assertEquals(Verdict.ALLOWED, verdict);
        assertTrue(verdict.allowed());
    }

    @Test
    void rejectsWhenShuffleDisabled() {
        Facts facts = new Facts();
        facts.shuffleEnabled = false;

        assertEquals(Verdict.SHUFFLE_DISABLED, facts.evaluate());
    }

    @Test
    void rejectsStaleContainer() {
        Facts facts = new Facts();
        facts.containerMatches = false;

        assertEquals(Verdict.STALE_CONTAINER, facts.evaluate());
    }

    @Test
    void rejectsNonVillagerTrader() {
        Facts facts = new Facts();
        facts.villager = false;

        assertEquals(Verdict.NOT_A_VILLAGER, facts.evaluate());
    }

    @Test
    void rejectsWhenVillagerTradesWithSomeoneElse() {
        Facts facts = new Facts();
        facts.tradingWithPlayer = false;

        assertEquals(Verdict.NOT_TRADING_WITH_PLAYER, facts.evaluate());
    }

    @Test
    void rejectsWhenTradedFlagSet() {
        Facts facts = new Facts();
        facts.traded = true;

        assertEquals(Verdict.ALREADY_TRADED, facts.evaluate());
    }

    @Test
    void rejectsVillagerWithXpEvenWithoutTradedFlag() {
        Facts facts = new Facts();
        facts.villagerXp = 2;

        assertEquals(Verdict.HAS_XP, facts.evaluate());
    }

    @Test
    void rejectsVillagerAboveNoviceLevel() {
        Facts facts = new Facts();
        facts.villagerLevel = 3;

        assertEquals(Verdict.NOT_NOVICE, facts.evaluate());
    }

    @Test
    void rejectsProfessionWithoutNoviceTrades() {
        Facts facts = new Facts();
        facts.hasNoviceTrades = false;

        assertEquals(Verdict.NO_TRADES, facts.evaluate());
    }

    @Test
    void rejectsMissingJobSiteWhenRequired() {
        Facts facts = new Facts();
        facts.hasJobSite = false;

        assertEquals(Verdict.NO_JOB_SITE, facts.evaluate());
    }

    @Test
    void allowsMissingJobSiteWhenNotRequired() {
        Facts facts = new Facts();
        facts.hasJobSite = false;
        facts.requireJobSite = false;

        assertEquals(Verdict.ALLOWED, facts.evaluate());
    }

    @Test
    void rejectsDuringCooldown() {
        Facts facts = new Facts();
        facts.cooldownElapsed = false;

        Verdict verdict = facts.evaluate();

        assertEquals(Verdict.COOLDOWN, verdict);
        assertFalse(verdict.allowed());
    }

    @Test
    void reportsFirstFailingRuleInOrder() {
        Facts facts = new Facts();
        facts.traded = true;
        facts.villagerXp = 5;
        facts.villagerLevel = 2;
        facts.cooldownElapsed = false;

        assertEquals(Verdict.ALREADY_TRADED, facts.evaluate());

        facts.shuffleEnabled = false;
        assertEquals(Verdict.SHUFFLE_DISABLED, facts.evaluate());
    }

    @Test
    void onlyAllowedVerdictAllows() {
        for (Verdict verdict : Verdict.values()) {
            assertEquals(verdict == Verdict.ALLOWED, verdict.allowed(), verdict.name());
        }
    }
}
