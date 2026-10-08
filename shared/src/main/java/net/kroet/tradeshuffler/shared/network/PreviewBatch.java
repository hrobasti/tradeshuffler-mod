package net.kroet.tradeshuffler.shared.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.trading.MerchantOffers;

/**
 * Offers a villager unlocks when it reaches {@code level}.
 */
public record PreviewBatch(int level, MerchantOffers offers) {
    public static final StreamCodec<RegistryFriendlyByteBuf, PreviewBatch> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            PreviewBatch::level,
            MerchantOffers.STREAM_CODEC,
            PreviewBatch::offers,
            PreviewBatch::new);
}
