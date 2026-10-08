package net.kroet.tradeshuffler.shared.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.kroet.tradeshuffler.common.rules.LevelPlan;
import net.minecraft.world.item.trading.MerchantOffers;

/**
 * Save format of a villager's trade plan:
 * {@code {profession, type, from_level, batches: [{level, offers}]}}, with
 * offers in vanilla's own format.
 */
public final class TradePlanCodec {
    private record Batch(int level, MerchantOffers offers) {
        static final Codec<Batch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("level").forGetter(Batch::level),
                MerchantOffers.CODEC.fieldOf("offers").forGetter(Batch::offers))
                .apply(instance, Batch::new));
    }

    private record Stored(String profession, String type, int fromLevel, List<Batch> batches) {
        static final Codec<Stored> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("profession").forGetter(Stored::profession),
                Codec.STRING.fieldOf("type").forGetter(Stored::type),
                Codec.INT.fieldOf("from_level").forGetter(Stored::fromLevel),
                Batch.CODEC.listOf().fieldOf("batches").forGetter(Stored::batches))
                .apply(instance, Stored::new));

        static Stored of(LevelPlan<MerchantOffers> plan) {
            List<Batch> batches = plan.batches().entrySet().stream().map(entry -> new Batch(entry.getKey(), entry.getValue())).toList();
            return new Stored(plan.stamp().profession(), plan.stamp().type(), plan.fromLevel(), batches);
        }

        LevelPlan<MerchantOffers> toPlan() {
            Map<Integer, MerchantOffers> byLevel = batches.stream().collect(Collectors.toMap(Batch::level, Batch::offers, (first, second) -> first));
            return LevelPlan.restore(new LevelPlan.Stamp(profession, type), fromLevel, byLevel);
        }
    }

    public static final Codec<LevelPlan<MerchantOffers>> CODEC = Stored.CODEC.xmap(Stored::toPlan, Stored::of);

    private TradePlanCodec() {
    }
}
