package net.kroet.tradeshuffler.shared.server;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;

/**
 * Carries the trade plan and traded flag through zombie infection and cure.
 * Vanilla only copies offers and XP, so without this a cure would roll a new
 * preview and allow shuffling a villager that was already traded with.
 */
public final class ZombieConversion {
    private ZombieConversion() {
    }

    public static void onConverted(Mob from, Mob to) {
        if (from instanceof Villager villager && to instanceof ZombieVillager zombie) {
            TradeShufflerVillager source = (TradeShufflerVillager) villager;
            TradeShufflerZombieVillager target = (TradeShufflerZombieVillager) zombie;
            target.tradeshuffler$setTraded(source.tradeshuffler$isTraded());
            target.tradeshuffler$setPlan(source.tradeshuffler$getPlan());
        } else if (from instanceof ZombieVillager zombie && to instanceof Villager villager) {
            TradeShufflerZombieVillager source = (TradeShufflerZombieVillager) zombie;
            TradeShufflerVillager target = (TradeShufflerVillager) villager;
            if (source.tradeshuffler$isTraded()) {
                target.tradeshuffler$markTraded();
            }
            target.tradeshuffler$setPlan(source.tradeshuffler$getPlan());
        }
    }
}
