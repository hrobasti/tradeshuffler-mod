package net.kroet.tradeshuffler.shared.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Compile-checked access to vanilla's trade generation and to the raw offers
 * field. Reading the field avoids {@code getOffers()}, which would generate
 * offers as a side effect.
 */
@Mixin(AbstractVillager.class)
public interface AbstractVillagerInvoker {
    @Invoker("addOffersFromTradeSet")
    void tradeshuffler$invokeAddOffersFromTradeSet(ServerLevel level, MerchantOffers offers, ResourceKey<TradeSet> tradeSet);

    @Accessor("offers")
    @Nullable
    MerchantOffers tradeshuffler$getOffersField();
}
