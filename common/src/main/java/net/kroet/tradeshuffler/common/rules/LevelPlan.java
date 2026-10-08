package net.kroet.tradeshuffler.common.rules;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Pre-generated trade batches for the levels a villager has not reached yet,
 * from {@link #fromLevel()} up to {@link #MAX_LEVEL}. The stamp ties the plan
 * to the profession and villager type it was generated for. Not thread-safe:
 * used from the server thread only.
 *
 * @param <T>
 *            batch type, e.g. Minecraft's merchant offers
 */
public final class LevelPlan<T> {
    public static final int MAX_LEVEL = 5;

    /** Profession and villager type the plan was generated for. */
    public record Stamp(String profession, String type) {
        public Stamp {
            Objects.requireNonNull(profession, "profession");
            Objects.requireNonNull(type, "type");
        }
    }

    private final Stamp stamp;
    private final TreeMap<Integer, T> batches = new TreeMap<>();
    private int fromLevel;

    /** Empty plan for a villager at {@code currentLevel}. */
    public LevelPlan(Stamp stamp, int currentLevel) {
        this.stamp = Objects.requireNonNull(stamp, "stamp");
        this.fromLevel = Math.max(2, Math.min(currentLevel, MAX_LEVEL) + 1);
    }

    /** Rebuilds a stored plan; batches outside the plan's range are dropped. */
    public static <T> LevelPlan<T> restore(Stamp stamp, int fromLevel, Map<Integer, T> batches) {
        LevelPlan<T> plan = new LevelPlan<>(stamp, fromLevel - 1);
        batches.forEach((level, batch) -> {
            if (level >= plan.fromLevel && level <= MAX_LEVEL && batch != null) {
                plan.batches.put(level, batch);
            }
        });
        return plan;
    }

    public Stamp stamp() {
        return stamp;
    }

    /** Lowest level the plan covers. */
    public int fromLevel() {
        return fromLevel;
    }

    /**
     * Whether the plan still fits the villager. A lower level than planned for (set
     * by commands) invalidates it, because batches for the levels to come again
     * were already dropped.
     */
    public boolean isValidFor(Stamp currentStamp, int currentLevel) {
        return stamp.equals(currentStamp) && currentLevel + 1 >= fromLevel;
    }

    /** First level in range without a batch, empty when the plan is complete. */
    public OptionalInt nextMissingLevel() {
        for (int level = fromLevel; level <= MAX_LEVEL; level++) {
            if (!batches.containsKey(level)) {
                return OptionalInt.of(level);
            }
        }
        return OptionalInt.empty();
    }

    public boolean isComplete() {
        return nextMissingLevel().isEmpty();
    }

    public void put(int level, T batch) {
        if (level < fromLevel || level > MAX_LEVEL) {
            throw new IllegalArgumentException("Level " + level + " outside plan range " + fromLevel + ".." + MAX_LEVEL);
        }
        batches.put(level, Objects.requireNonNull(batch, "batch"));
    }

    /**
     * Removes and returns the batch for a level the villager just reached; all
     * batches up to that level are dropped.
     */
    public Optional<T> take(int level) {
        Optional<T> batch = Optional.ofNullable(batches.get(level));
        dropUpTo(level);
        return batch;
    }

    /** Drops all batches up to and including {@code level}. */
    public void dropUpTo(int level) {
        batches.headMap(level, true).clear();
        fromLevel = Math.max(fromLevel, Math.min(level, MAX_LEVEL) + 1);
    }

    /** Batches by level, in ascending order. */
    public SortedMap<Integer, T> batches() {
        return Collections.unmodifiableSortedMap(batches);
    }
}
