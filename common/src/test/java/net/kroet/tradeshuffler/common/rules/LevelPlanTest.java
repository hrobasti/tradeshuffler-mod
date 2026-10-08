package net.kroet.tradeshuffler.common.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.kroet.tradeshuffler.common.rules.LevelPlan.Stamp;
import org.junit.jupiter.api.Test;

class LevelPlanTest {
    private static final Stamp LIBRARIAN = new Stamp("minecraft:librarian", "minecraft:plains");

    private static LevelPlan<String> completePlanFromNovice() {
        LevelPlan<String> plan = new LevelPlan<>(LIBRARIAN, 1);
        for (int level = 2; level <= LevelPlan.MAX_LEVEL; level++) {
            plan.put(level, "batch" + level);
        }
        return plan;
    }

    @Test
    void newPlanCoversLevelsAboveCurrentInOrder() {
        LevelPlan<String> plan = new LevelPlan<>(LIBRARIAN, 1);

        assertEquals(2, plan.fromLevel());
        assertEquals(OptionalInt.of(2), plan.nextMissingLevel());
        plan.put(2, "batch2");
        assertEquals(OptionalInt.of(3), plan.nextMissingLevel());
        assertFalse(plan.isComplete());
    }

    @Test
    void planIsCompleteWhenAllFutureLevelsHaveBatches() {
        LevelPlan<String> plan = completePlanFromNovice();

        assertTrue(plan.isComplete());
        assertEquals(OptionalInt.empty(), plan.nextMissingLevel());
        assertEquals(List.of(2, 3, 4, 5), List.copyOf(plan.batches().keySet()));
    }

    @Test
    void masterVillagerPlanIsCompleteRightAway() {
        LevelPlan<String> plan = new LevelPlan<>(LIBRARIAN, LevelPlan.MAX_LEVEL);

        assertTrue(plan.isComplete());
        assertTrue(plan.batches().isEmpty());
    }

    @Test
    void putRejectsLevelsOutsideRange() {
        LevelPlan<String> plan = new LevelPlan<>(LIBRARIAN, 2);

        assertThrows(IllegalArgumentException.class, () -> plan.put(2, "batch2"));
        assertThrows(IllegalArgumentException.class, () -> plan.put(6, "batch6"));
    }

    @Test
    void takeReturnsBatchAndDropsReachedLevels() {
        LevelPlan<String> plan = completePlanFromNovice();

        assertEquals(Optional.of("batch2"), plan.take(2));
        assertEquals(3, plan.fromLevel());
        assertEquals(List.of(3, 4, 5), List.copyOf(plan.batches().keySet()));
        assertTrue(plan.isComplete());
    }

    @Test
    void takeOfMissingLevelReturnsEmptyAndStillDrops() {
        LevelPlan<String> plan = new LevelPlan<>(LIBRARIAN, 1);
        plan.put(3, "batch3");

        assertEquals(Optional.empty(), plan.take(2));
        assertEquals(3, plan.fromLevel());
        assertEquals(Optional.of("batch3"), plan.take(3));
    }

    @Test
    void levelJumpDropsSkippedBatches() {
        LevelPlan<String> plan = completePlanFromNovice();

        plan.dropUpTo(3);

        assertEquals(4, plan.fromLevel());
        assertEquals(List.of(4, 5), List.copyOf(plan.batches().keySet()));
        assertTrue(plan.isComplete());
    }

    @Test
    void dropUpToNeverMovesRangeBackwards() {
        LevelPlan<String> plan = completePlanFromNovice();
        plan.dropUpTo(4);

        plan.dropUpTo(2);

        assertEquals(5, plan.fromLevel());
    }

    @Test
    void stampMismatchInvalidates() {
        LevelPlan<String> plan = completePlanFromNovice();

        assertTrue(plan.isValidFor(LIBRARIAN, 1));
        assertFalse(plan.isValidFor(new Stamp("minecraft:cleric", "minecraft:plains"), 1));
        assertFalse(plan.isValidFor(new Stamp("minecraft:librarian", "minecraft:desert"), 1));
    }

    @Test
    void higherLevelStaysValidButLowerLevelInvalidates() {
        LevelPlan<String> plan = completePlanFromNovice();
        plan.take(2);

        assertTrue(plan.isValidFor(LIBRARIAN, 2));
        assertTrue(plan.isValidFor(LIBRARIAN, 4));
        assertFalse(plan.isValidFor(LIBRARIAN, 1));
    }

    @Test
    void restoreKeepsOnlyBatchesInRange() {
        LevelPlan<String> plan = LevelPlan.restore(LIBRARIAN, 3, Map.of(2, "batch2", 3, "batch3", 4, "batch4", 5, "batch5", 6, "batch6"));

        assertEquals(3, plan.fromLevel());
        assertEquals(LIBRARIAN, plan.stamp());
        assertEquals(List.of(3, 4, 5), List.copyOf(plan.batches().keySet()));
        assertTrue(plan.isComplete());
    }

    @Test
    void batchesViewIsReadOnly() {
        LevelPlan<String> plan = completePlanFromNovice();

        assertThrows(UnsupportedOperationException.class, () -> plan.batches().clear());
    }

    @Test
    void restoreNeverStartsBelowApprentice() {
        LevelPlan<String> plan = LevelPlan.restore(LIBRARIAN, 0, Map.of(1, "batch1", 2, "batch2"));

        assertEquals(2, plan.fromLevel());
        assertEquals(List.of(2), List.copyOf(plan.batches().keySet()));
    }

    @Test
    void takingTheMasterBatchLeavesNothingToPreview() {
        LevelPlan<String> plan = new LevelPlan<>(LIBRARIAN, 4);
        plan.put(5, "batch5");

        assertEquals(Optional.of("batch5"), plan.take(5));
        assertEquals(LevelPlan.MAX_LEVEL + 1, plan.fromLevel());
        assertTrue(plan.isComplete());
        assertTrue(plan.batches().isEmpty());
    }
}
