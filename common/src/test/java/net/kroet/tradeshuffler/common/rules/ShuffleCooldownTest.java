package net.kroet.tradeshuffler.common.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ShuffleCooldownTest {
    @Test
    void unknownPlayerIsElapsed() {
        assertTrue(new ShuffleCooldown<String>().isElapsed("alex", 100, 10));
    }

    @Test
    void blocksUntilCooldownHasPassed() {
        ShuffleCooldown<String> cooldown = new ShuffleCooldown<>();
        cooldown.markUsed("alex", 100);

        assertFalse(cooldown.isElapsed("alex", 100, 10));
        assertFalse(cooldown.isElapsed("alex", 109, 10));
        assertTrue(cooldown.isElapsed("alex", 110, 10));
    }

    @Test
    void zeroCooldownNeverBlocks() {
        ShuffleCooldown<String> cooldown = new ShuffleCooldown<>();
        cooldown.markUsed("alex", 100);

        assertTrue(cooldown.isElapsed("alex", 100, 0));
    }

    @Test
    void playersAreTrackedSeparately() {
        ShuffleCooldown<String> cooldown = new ShuffleCooldown<>();
        cooldown.markUsed("alex", 100);

        assertTrue(cooldown.isElapsed("steve", 101, 10));
    }

    @Test
    void tickCounterGoingBackwardsDoesNotBlock() {
        ShuffleCooldown<String> cooldown = new ShuffleCooldown<>();
        cooldown.markUsed("alex", 5000);

        assertTrue(cooldown.isElapsed("alex", 3, 10));
    }

    @Test
    void forgetAndClearResetPlayers() {
        ShuffleCooldown<String> cooldown = new ShuffleCooldown<>();
        cooldown.markUsed("alex", 100);
        cooldown.markUsed("steve", 100);

        cooldown.forget("alex");
        assertTrue(cooldown.isElapsed("alex", 101, 10));
        assertFalse(cooldown.isElapsed("steve", 101, 10));

        cooldown.clear();
        assertTrue(cooldown.isElapsed("steve", 101, 10));
    }
}
