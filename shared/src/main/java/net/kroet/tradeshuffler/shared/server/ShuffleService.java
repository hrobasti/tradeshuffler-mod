package net.kroet.tradeshuffler.shared.server;

import net.kroet.tradeshuffler.shared.mixin.MerchantMenuAccessor;
import net.kroet.tradeshuffler.shared.mixin.VillagerInvoker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;

/**
 * Replaces a villager's offers with a freshly generated novice batch. Runs on
 * the server thread after {@link ShuffleRequestHandler} accepted the request.
 */
final class ShuffleService {
    private ShuffleService() {
    }

    static void shuffle(ServerPlayer player, Villager villager, MerchantMenu menu) {
        // Vanilla generates the novice batch through the regular trade set path, so
        // data packs and modded trades apply.
        villager.setOffers(null);
        MerchantOffers offers = villager.getOffers();
        // For non-empty offers this also resends them and recomputes the result slot,
        // so a selected old offer cannot be taken for free.
        ((VillagerInvoker) villager).tradeshuffler$invokeUpdateSpecialPrices(player);
        if (offers.isEmpty()) {
            player.sendMerchantOffers(
                    menu.containerId,
                    offers,
                    villager.getVillagerData().level(),
                    villager.getVillagerXp(),
                    villager.showProgressBar(),
                    villager.canRestock());
            menu.slotsChanged(((MerchantMenuAccessor) menu).tradeshuffler$getTradeContainer());
        }
        // Regenerating the offers discarded the old plan; the new one is rolled once
        // shuffling pauses.
        TradePlanService.scheduleAfterShuffle(villager);
        TradeStateService.sendTradeState(player, villager);
    }
}
