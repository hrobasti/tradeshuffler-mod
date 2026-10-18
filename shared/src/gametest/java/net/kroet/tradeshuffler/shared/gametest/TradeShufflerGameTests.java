package net.kroet.tradeshuffler.shared.gametest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.kroet.tradeshuffler.common.rules.LevelPlan;
import net.kroet.tradeshuffler.common.rules.PlanPacer;
import net.kroet.tradeshuffler.shared.network.ShuffleRequestPayload;
import net.kroet.tradeshuffler.shared.server.ServerConfigHolder;
import net.kroet.tradeshuffler.shared.server.ShuffleRequestHandler;
import net.kroet.tradeshuffler.shared.server.TradePlanService;
import net.kroet.tradeshuffler.shared.server.TradeShufflerVillager;
import net.kroet.tradeshuffler.shared.server.TradeShufflerZombieVillager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Server game tests for TradeShuffler, shared by both loaders. They only use
 * vanilla's {@link GameTestHelper}; each loader registers them under the names
 * in {@link #ALL}.
 */
public final class TradeShufflerGameTests {
    /** Plan generation takes a few ticks; this leaves room for slow servers. */
    public static final int MAX_TICKS = 200;

    /** Test name to test function, in a stable order. */
    public static final Map<String, Consumer<GameTestHelper>> ALL;

    private static final BlockPos VILLAGER_POS = new BlockPos(2, 1, 2);
    private static final BlockPos JOB_SITE_POS = new BlockPos(5, 1, 2);
    private static final BlockPos PLAYER_POS = new BlockPos(3, 1, 2);
    private static final BlockPos ZOMBIE_POS = new BlockPos(5, 1, 5);
    private static final int RAPID_SHUFFLES = 5;

    static {
        Map<String, Consumer<GameTestHelper>> tests = new LinkedHashMap<>();
        tests.put("zombie_cure_keeps_plan_and_traded_flag", TradeShufflerGameTests::zombieCureKeepsPlanAndTradedFlag);
        tests.put("zombie_cure_keeps_untraded_villager_untraded", TradeShufflerGameTests::zombieCureKeepsUntradedVillagerUntraded);
        tests.put("level_up_unlocks_planned_trades", TradeShufflerGameTests::levelUpUnlocksPlannedTrades);
        tests.put("profession_change_discards_plan", TradeShufflerGameTests::professionChangeDiscardsPlan);
        tests.put("plan_and_traded_flag_survive_saving", TradeShufflerGameTests::planAndTradedFlagSurviveSaving);
        tests.put("shuffle_request_rerolls_until_first_trade", TradeShufflerGameTests::shuffleRequestRerollsUntilFirstTrade);
        tests.put("rapid_shuffles_generate_plan_once_settled", TradeShufflerGameTests::rapidShufflesGeneratePlanOnceSettled);
        tests.put("zero_xp_trade_locks_shuffling", TradeShufflerGameTests::zeroXpTradeLocksShuffling);
        ALL = Collections.unmodifiableMap(tests);
    }

    private TradeShufflerGameTests() {
    }

    public static void zombieCureKeepsPlanAndTradedFlag(GameTestHelper helper) {
        zombieRoundTrip(helper, true);
    }

    public static void zombieCureKeepsUntradedVillagerUntraded(GameTestHelper helper) {
        zombieRoundTrip(helper, false);
    }

    public static void levelUpUnlocksPlannedTrades(GameTestHelper helper) {
        Villager villager = plannedLibrarian(helper);
        helper.startSequence()
                .thenWaitUntil(() -> assertPlanComplete(helper, villager))
                .thenExecute(() -> {
                    Tag expected = encode(helper, plan(villager).batches().get(2));
                    int noviceCount = villager.getOffers().size();

                    villager.setVillagerXp(VillagerData.getMaxXpPerLevel(1));
                    villager.notifyTrade(villager.getOffers().get(0));

                    helper.assertValueEqual(villager.getVillagerData().level(), 2, "villager level");
                    MerchantOffers unlocked = new MerchantOffers();
                    MerchantOffers offers = villager.getOffers();
                    for (int i = noviceCount; i < offers.size(); i++) {
                        unlocked.add(offers.get(i));
                    }
                    helper.assertValueEqual(encode(helper, unlocked), expected, "trades unlocked at level 2");
                })
                .thenSucceed();
    }

    public static void professionChangeDiscardsPlan(GameTestHelper helper) {
        Villager villager = plannedLibrarian(helper);
        helper.startSequence()
                .thenWaitUntil(() -> assertPlanComplete(helper, villager))
                .thenExecute(() -> {
                    villager.setVillagerData(villager.getVillagerData()
                            .withProfession(helper.getLevel().registryAccess(), VillagerProfession.FARMER));
                    villager.getOffers();
                    helper.assertTrue(plan(villager) == null, "trade plan kept after a profession change");
                })
                .thenSucceed();
    }

    public static void planAndTradedFlagSurviveSaving(GameTestHelper helper) {
        Villager villager = plannedLibrarian(helper);
        helper.startSequence()
                .thenWaitUntil(() -> assertPlanComplete(helper, villager))
                .thenExecute(() -> {
                    ((TradeShufflerVillager) villager).tradeshuffler$markTraded();
                    Tag expected = encodePlan(helper, plan(villager));

                    TradeShufflerVillager loaded = (TradeShufflerVillager) reload(helper, villager, EntityTypes.VILLAGER);
                    helper.assertTrue(loaded.tradeshuffler$isTraded(), "traded flag lost when saving a villager");
                    helper.assertValueEqual(encodePlan(helper, loaded.tradeshuffler$getPlan()), expected, "trade plan after saving a villager");

                    ZombieVillager zombie = infect(helper, villager);
                    TradeShufflerZombieVillager loadedZombie = (TradeShufflerZombieVillager) reload(helper, zombie, EntityTypes.ZOMBIE_VILLAGER);
                    helper.assertTrue(loadedZombie.tradeshuffler$isTraded(), "traded flag lost when saving a zombie villager");
                    helper.assertValueEqual(encodePlan(helper, loadedZombie.tradeshuffler$getPlan()), expected,
                            "trade plan after saving a zombie villager");
                })
                .thenSucceed();
    }

    public static void shuffleRequestRerollsUntilFirstTrade(GameTestHelper helper) {
        Villager villager = librarian(helper);
        ServerPlayer player = openTrading(helper, villager);
        int containerId = player.containerMenu.containerId;

        MerchantOffers beforeShuffle = villager.getOffers();
        ShuffleRequestHandler.handle(player, new ShuffleRequestPayload(containerId));
        helper.assertTrue(villager.getOffers() != beforeShuffle, "allowed shuffle request did not reroll the trades");

        villager.notifyTrade(villager.getOffers().get(0));
        helper.runAfterDelay(ServerConfigHolder.current().shuffleCooldownTicks() + 1, () -> {
            assertStillTrading(helper, player, containerId);
            MerchantOffers afterTrade = villager.getOffers();
            ShuffleRequestHandler.handle(player, new ShuffleRequestPayload(containerId));
            helper.assertTrue(villager.getOffers() == afterTrade, "shuffle request after the first trade was accepted");
            helper.succeed();
        });
    }

    /** Data-pack trades may reward no XP; the first trade still locks shuffling. */
    public static void zeroXpTradeLocksShuffling(GameTestHelper helper) {
        Villager villager = librarian(helper);
        ServerPlayer player = openTrading(helper, villager);
        int containerId = player.containerMenu.containerId;

        MerchantOffer zeroXpTrade = new MerchantOffer(new ItemCost(Items.EMERALD), new ItemStack(Items.PAPER), 12, 0, 0.05F);
        villager.getOffers().add(zeroXpTrade);
        villager.notifyTrade(zeroXpTrade);
        helper.assertValueEqual(villager.getVillagerXp(), 0, "villager XP after a 0 XP trade");
        helper.assertTrue(((TradeShufflerVillager) villager).tradeshuffler$isTraded(), "0 XP trade did not mark the villager as traded");
        villager.restock();
        helper.assertTrue(((TradeShufflerVillager) villager).tradeshuffler$isTraded(), "traded flag lost after a restock");

        helper.runAfterDelay(ServerConfigHolder.current().shuffleCooldownTicks() + 1, () -> {
            assertStillTrading(helper, player, containerId);
            MerchantOffers afterTrade = villager.getOffers();
            ShuffleRequestHandler.handle(player, new ShuffleRequestPayload(containerId));
            helper.assertTrue(villager.getOffers() == afterTrade, "shuffle request after a 0 XP trade was accepted");
            helper.succeed();
        });
    }

    /**
     * Rapid shuffles at a cartographer roll the plan, and with it the exploration
     * maps, once after shuffling pauses instead of after every shuffle.
     */
    public static void rapidShufflesGeneratePlanOnceSettled(GameTestHelper helper) {
        Villager villager = novice(helper, VillagerProfession.CARTOGRAPHER, Blocks.CARTOGRAPHY_TABLE);
        ServerPlayer player = openTrading(helper, villager);
        int containerId = player.containerMenu.containerId;
        // Each call claims an id, so maps rolled in between show as a gap.
        int firstMapId = helper.getLevel().getFreeMapId().id();

        GameTestSequence sequence = helper.startSequence();
        for (int i = 0; i < RAPID_SHUFFLES; i++) {
            sequence.thenExecuteAfter(ServerConfigHolder.current().shuffleCooldownTicks() + 1, () -> {
                helper.assertTrue(plan(villager) == null, "trade plan generated while shuffling");
                assertStillTrading(helper, player, containerId);
                MerchantOffers beforeShuffle = villager.getOffers();
                ShuffleRequestHandler.handle(player, new ShuffleRequestPayload(containerId));
                helper.assertTrue(villager.getOffers() != beforeShuffle, "shuffle request did not reroll the trades");
            });
        }
        sequence.thenExecuteAfter(PlanPacer.SHUFFLE_SETTLE_TICKS - 5,
                () -> helper.assertTrue(plan(villager) == null, "trade plan generated before shuffling paused"))
                .thenWaitUntil(() -> assertPlanComplete(helper, villager))
                .thenExecute(() -> {
                    int mapsInPlan = 0;
                    for (MerchantOffers batch : plan(villager).batches().values()) {
                        for (MerchantOffer offer : batch) {
                            if (offer.getResult().has(DataComponents.MAP_ID)) {
                                mapsInPlan++;
                            }
                        }
                    }
                    int mapsRolled = helper.getLevel().getFreeMapId().id() - firstMapId - 1;
                    helper.assertValueEqual(mapsRolled, mapsInPlan, "exploration maps rolled");
                })
                .thenSucceed();
    }

    private static void zombieRoundTrip(GameTestHelper helper, boolean traded) {
        Villager villager = plannedLibrarian(helper);
        if (traded) {
            ((TradeShufflerVillager) villager).tradeshuffler$markTraded();
        }
        helper.startSequence()
                .thenWaitUntil(() -> assertPlanComplete(helper, villager))
                .thenExecute(() -> {
                    Tag expected = encodePlan(helper, plan(villager));

                    ZombieVillager zombie = infect(helper, villager);
                    TradeShufflerZombieVillager zombieState = (TradeShufflerZombieVillager) zombie;
                    helper.assertValueEqual(zombieState.tradeshuffler$isTraded(), traded, "traded flag on the zombie villager");
                    helper.assertValueEqual(encodePlan(helper, zombieState.tradeshuffler$getPlan()), expected, "trade plan on the zombie villager");

                    Villager cured = zombie.convertTo(EntityTypes.VILLAGER, ConversionParams.single(zombie, false, false),
                            villagerAfterCure -> villagerAfterCure.setVillagerData(zombie.getVillagerData()));
                    helper.assertTrue(cured != null, "zombie villager was not cured");
                    TradeShufflerVillager curedState = (TradeShufflerVillager) cured;
                    helper.assertValueEqual(curedState.tradeshuffler$isTraded(), traded, "traded flag after the cure");
                    helper.assertValueEqual(encodePlan(helper, curedState.tradeshuffler$getPlan()), expected, "trade plan after the cure");
                })
                .thenSucceed();
    }

    private static ZombieVillager infect(GameTestHelper helper, Villager villager) {
        Zombie zombie = helper.spawn(EntityTypes.ZOMBIE, ZOMBIE_POS);
        zombie.setNoAi(true);
        helper.assertTrue(zombie.convertVillagerToZombieVillager(helper.getLevel(), villager), "villager did not turn into a zombie villager");
        return helper.findOneEntity(EntityTypes.ZOMBIE_VILLAGER);
    }

    private static Villager librarian(GameTestHelper helper) {
        return novice(helper, VillagerProfession.LIBRARIAN, Blocks.LECTERN);
    }

    /** Novice with a job site, standing on a floor. */
    private static Villager novice(GameTestHelper helper, ResourceKey<VillagerProfession> profession, Block jobSite) {
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
        helper.setBlock(JOB_SITE_POS, jobSite);
        Villager villager = helper.spawn(EntityTypes.VILLAGER, VILLAGER_POS);
        // Without AI the brain neither validates the job site nor fires the novice;
        // the entity still ticks, so the trade plan is generated as usual.
        villager.setNoAi(true);
        villager.setVillagerData(villager.getVillagerData()
                .withProfession(helper.getLevel().registryAccess(), profession)
                .withLevel(1));
        // The shuffle check requires a remembered job site.
        villager.getBrain().setMemory(MemoryModuleType.JOB_SITE, GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(JOB_SITE_POS)));
        return villager;
    }

    @SuppressWarnings("removal")
    private static ServerPlayer openTrading(GameTestHelper helper, Villager villager) {
        // The in-level mock player has a network connection, which opening the
        // trading screen and resending offers need.
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // The trading screen closes on the next tick when the player is out of reach.
        player.snapTo(helper.absoluteVec(Vec3.atBottomCenterOf(PLAYER_POS)));
        villager.setTradingPlayer(player);
        villager.openTradingScreen(player, villager.getDisplayName(), 1);
        helper.assertTrue(player.containerMenu instanceof MerchantMenu, "trading screen did not open");
        return player;
    }

    /** Guards against a request rejected only because the screen was closed. */
    private static void assertStillTrading(GameTestHelper helper, ServerPlayer player, int containerId) {
        helper.assertTrue(player.containerMenu instanceof MerchantMenu && player.containerMenu.containerId == containerId,
                "trading screen was closed");
    }

    private static Villager plannedLibrarian(GameTestHelper helper) {
        Villager villager = librarian(helper);
        villager.getOffers();
        TradePlanService.schedule(villager);
        return villager;
    }

    private static void assertPlanComplete(GameTestHelper helper, Villager villager) {
        LevelPlan<MerchantOffers> plan = plan(villager);
        helper.assertTrue(plan != null && plan.isComplete(), "trade plan was not generated");
    }

    private static LevelPlan<MerchantOffers> plan(Villager villager) {
        return ((TradeShufflerVillager) villager).tradeshuffler$getPlan();
    }

    private static Tag encode(GameTestHelper helper, MerchantOffers offers) {
        return MerchantOffers.CODEC.encodeStart(helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE), offers)
                .getOrThrow();
    }

    private static Tag encodePlan(GameTestHelper helper, LevelPlan<MerchantOffers> plan) {
        CompoundTag tag = new CompoundTag();
        if (plan != null) {
            tag.putInt("from_level", plan.fromLevel());
            plan.batches().forEach((level, offers) -> tag.put(Integer.toString(level), encode(helper, offers)));
        }
        return tag;
    }

    private static <E extends Entity> E reload(GameTestHelper helper, E entity, EntityType<E> type) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        entity.saveWithoutId(output);
        E copy = type.create(helper.getLevel(), EntitySpawnReason.LOAD);
        helper.assertTrue(copy != null, "could not create " + type);
        copy.load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), output.buildResult()));
        return copy;
    }
}
