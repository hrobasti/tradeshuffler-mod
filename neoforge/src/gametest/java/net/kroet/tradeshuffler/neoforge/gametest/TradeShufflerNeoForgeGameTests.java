package net.kroet.tradeshuffler.neoforge.gametest;

import java.util.function.Consumer;
import net.kroet.tradeshuffler.shared.gametest.TradeShufflerGameTests;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers the shared TradeShuffler game tests with NeoForge: the functions in
 * vanilla's test function registry, the tests through
 * {@link RegisterGameTestsEvent}.
 */
@Mod(TradeShufflerNeoForgeGameTests.MOD_ID)
public final class TradeShufflerNeoForgeGameTests {
    static final String MOD_ID = "tradeshuffler_gametest";

    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, MOD_ID);

    static {
        TradeShufflerGameTests.ALL.forEach((name, function) -> FUNCTIONS.register(name, () -> function));
    }

    public TradeShufflerNeoForgeGameTests(IEventBus modEventBus) {
        FUNCTIONS.register(modEventBus);
        modEventBus.addListener(TradeShufflerNeoForgeGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("default"));
        Identifier emptyStructure = id("empty");
        for (String name : TradeShufflerGameTests.ALL.keySet()) {
            event.registerTest(id(name), new FunctionGameTestInstance(
                    ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                    new TestData<>(environment, emptyStructure, TradeShufflerGameTests.MAX_TICKS, 0, true)));
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
