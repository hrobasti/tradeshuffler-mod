package net.kroet.tradeshuffler.shared.network;

import java.util.List;
import net.kroet.tradeshuffler.shared.TradeShufflerConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server tells the client whether the villager in the open merchant menu may be
 * shuffled, together with the preview of its future levels, so the client
 * always has one consistent state. The block reason is a
 * {@code ShuffleEligibility.Verdict} name, empty when shuffling is allowed; the
 * client turns it into text.
 */
public record TradeStatePayload(int containerId, String shuffleBlockReason, List<PreviewBatch> preview) implements CustomPacketPayload {
    /** Levels 2 to 5 at most. */
    public static final int MAX_PREVIEW_BATCHES = 4;
    private static final int MAX_REASON_LENGTH = 64;

    public static final CustomPacketPayload.Type<TradeStatePayload> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(TradeShufflerConstants.MOD_ID, "trade_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TradeStatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            TradeStatePayload::containerId,
            ByteBufCodecs.stringUtf8(MAX_REASON_LENGTH),
            TradeStatePayload::shuffleBlockReason,
            PreviewBatch.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_PREVIEW_BATCHES)),
            TradeStatePayload::preview,
            TradeStatePayload::new);

    public TradeStatePayload {
        preview = List.copyOf(preview);
    }

    public boolean shuffleAllowed() {
        return shuffleBlockReason.isEmpty();
    }

    @Override
    public CustomPacketPayload.Type<TradeStatePayload> type() {
        return TYPE;
    }
}
