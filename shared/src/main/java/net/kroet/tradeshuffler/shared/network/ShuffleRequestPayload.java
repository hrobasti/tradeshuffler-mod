package net.kroet.tradeshuffler.shared.network;

import io.netty.buffer.ByteBuf;
import net.kroet.tradeshuffler.shared.TradeShufflerConstants;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client asks the server to shuffle the trades of the villager in the open
 * merchant menu. The container id lets the server drop stale clicks.
 */
public record ShuffleRequestPayload(int containerId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ShuffleRequestPayload> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(TradeShufflerConstants.MOD_ID, "shuffle_request"));

    public static final StreamCodec<ByteBuf, ShuffleRequestPayload> STREAM_CODEC = ByteBufCodecs.VAR_INT
            .map(ShuffleRequestPayload::new, ShuffleRequestPayload::containerId);

    @Override
    public CustomPacketPayload.Type<ShuffleRequestPayload> type() {
        return TYPE;
    }
}
