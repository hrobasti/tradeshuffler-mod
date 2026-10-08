package net.kroet.tradeshuffler.fabric.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.kroet.tradeshuffler.shared.gametest.TradeShufflerGameTests;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Registers the shared TradeShuffler game tests with Fabric's game test API.
 */
public final class TradeShufflerFabricGameTests {
    @GameTest(maxTicks = TradeShufflerGameTests.MAX_TICKS)
    public void zombieCureKeepsPlanAndTradedFlag(GameTestHelper helper) {
        TradeShufflerGameTests.zombieCureKeepsPlanAndTradedFlag(helper);
    }

    @GameTest(maxTicks = TradeShufflerGameTests.MAX_TICKS)
    public void zombieCureKeepsUntradedVillagerUntraded(GameTestHelper helper) {
        TradeShufflerGameTests.zombieCureKeepsUntradedVillagerUntraded(helper);
    }

    @GameTest(maxTicks = TradeShufflerGameTests.MAX_TICKS)
    public void levelUpUnlocksPlannedTrades(GameTestHelper helper) {
        TradeShufflerGameTests.levelUpUnlocksPlannedTrades(helper);
    }

    @GameTest(maxTicks = TradeShufflerGameTests.MAX_TICKS)
    public void professionChangeDiscardsPlan(GameTestHelper helper) {
        TradeShufflerGameTests.professionChangeDiscardsPlan(helper);
    }

    @GameTest(maxTicks = TradeShufflerGameTests.MAX_TICKS)
    public void planAndTradedFlagSurviveSaving(GameTestHelper helper) {
        TradeShufflerGameTests.planAndTradedFlagSurviveSaving(helper);
    }

    @GameTest(maxTicks = TradeShufflerGameTests.MAX_TICKS)
    public void shuffleRequestRerollsUntilFirstTrade(GameTestHelper helper) {
        TradeShufflerGameTests.shuffleRequestRerollsUntilFirstTrade(helper);
    }

    @GameTest(maxTicks = TradeShufflerGameTests.MAX_TICKS)
    public void rapidShufflesGeneratePlanOnceSettled(GameTestHelper helper) {
        TradeShufflerGameTests.rapidShufflesGeneratePlanOnceSettled(helper);
    }
}
