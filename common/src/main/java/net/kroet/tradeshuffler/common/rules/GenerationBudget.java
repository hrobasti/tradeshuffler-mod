package net.kroet.tradeshuffler.common.rules;

/**
 * Server-wide limit of one trade-plan generation step per server tick, shared
 * by all villagers. Server thread only.
 */
public final class GenerationBudget {
    private long grantedTick = Long.MIN_VALUE;

    /** @return true for the first request in a tick, false for every later one */
    public boolean tryAcquire(long serverTick) {
        if (serverTick == grantedTick) {
            return false;
        }
        grantedTick = serverTick;
        return true;
    }

    public void reset() {
        grantedTick = Long.MIN_VALUE;
    }
}
