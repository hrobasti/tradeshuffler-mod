package net.kroet.tradeshuffler.common.rules;

/**
 * Decides when one villager may generate the next level of its trade plan.
 * After a shuffle generation waits until shuffling has paused for
 * {@link #SHUFFLE_SETTLE_TICKS}: generating runs vanilla trade functions such
 * as exploration maps, which search for structures and claim them.
 */
public final class PlanPacer {
    /** Shuffle pause before the preview of the kept result is generated (2 s). */
    public static final int SHUFFLE_SETTLE_TICKS = 40;

    private boolean pending;
    private int waitTicks;

    /** Generate as soon as possible, but never earlier than an open settle wait. */
    public void request() {
        pending = true;
    }

    /**
     * Generate once shuffling has paused; every further shuffle restarts the wait.
     */
    public void requestAfterShuffle() {
        pending = true;
        waitTicks = SHUFFLE_SETTLE_TICKS;
    }

    public void cancel() {
        pending = false;
        waitTicks = 0;
    }

    public boolean isPending() {
        return pending;
    }

    /**
     * Called once per villager tick.
     *
     * @return whether one generation step may run in this tick
     */
    public boolean tick(GenerationBudget budget, long serverTick) {
        if (!pending) {
            return false;
        }
        if (waitTicks > 0) {
            waitTicks--;
            return false;
        }
        return budget.tryAcquire(serverTick);
    }
}
