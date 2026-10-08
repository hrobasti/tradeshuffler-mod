package net.kroet.tradeshuffler.common.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlanPacerTest {
    private final GenerationBudget budget = new GenerationBudget();
    private long serverTick;

    private boolean tick(PlanPacer pacer) {
        return pacer.tick(budget, serverTick++);
    }

    @Test
    void idlePacerNeverGenerates() {
        PlanPacer pacer = new PlanPacer();

        for (int i = 0; i < 100; i++) {
            assertFalse(tick(pacer));
        }
    }

    @Test
    void requestGeneratesInTheNextTick() {
        PlanPacer pacer = new PlanPacer();
        pacer.request();

        assertTrue(tick(pacer));
    }

    @Test
    void shuffleWaitsForTheSettlePause() {
        PlanPacer pacer = new PlanPacer();
        pacer.requestAfterShuffle();

        for (int i = 0; i < PlanPacer.SHUFFLE_SETTLE_TICKS; i++) {
            assertFalse(tick(pacer));
        }
        assertTrue(tick(pacer));
    }

    @Test
    void rapidShufflingGeneratesOnlyAfterTheLastShuffle() {
        PlanPacer pacer = new PlanPacer();
        int steps = 0;
        int lastShuffleTick = 0;
        // 30 shuffles, one every 10 ticks (the default cooldown), then a pause.
        for (int i = 0; i < 300 + PlanPacer.SHUFFLE_SETTLE_TICKS + 1; i++) {
            if (i < 300 && i % 10 == 0) {
                pacer.requestAfterShuffle();
                lastShuffleTick = i;
            }
            if (tick(pacer)) {
                steps++;
                assertTrue(i > lastShuffleTick + PlanPacer.SHUFFLE_SETTLE_TICKS - 1);
                pacer.cancel();
            }
        }

        assertEquals(1, steps);
    }

    @Test
    void openingTheScreenDoesNotCutTheSettleWaitShort() {
        PlanPacer pacer = new PlanPacer();
        pacer.requestAfterShuffle();
        pacer.request();

        assertFalse(tick(pacer));
    }

    @Test
    void cancelStopsPendingGeneration() {
        PlanPacer pacer = new PlanPacer();
        pacer.request();
        pacer.cancel();

        assertFalse(pacer.isPending());
        assertFalse(tick(pacer));
    }

    @Test
    void budgetAllowsOneStepPerServerTickAcrossVillagers() {
        PlanPacer first = new PlanPacer();
        PlanPacer second = new PlanPacer();
        first.request();
        second.request();

        assertTrue(first.tick(budget, 7));
        assertFalse(second.tick(budget, 7));
        assertTrue(second.tick(budget, 8));
    }
}
