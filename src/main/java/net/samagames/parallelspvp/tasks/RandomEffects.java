package net.samagames.parallelspvp.tasks;

import net.samagames.parallelspvp.ParallelsPVP;
import net.samagames.parallelspvp.arena.Arena;
import net.samagames.parallelspvp.arena.DimensionsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;
import java.util.UUID;

/**
 * Created by zyuiop on 26/09/14.
 */
public class RandomEffects implements Runnable {

    protected Arena parent;
    protected int nextEffect = 60;
    protected PotionEffect[] effects;

    public RandomEffects(Arena parentArena) {
        this.parent = parentArena;
        effects = new PotionEffect[] {
                new PotionEffect(PotionEffectType.HARM, 1, 0),
                new PotionEffect(PotionEffectType.BLINDNESS, 60, 0),
                new PotionEffect(PotionEffectType.SLOW, 100, 0),
                new PotionEffect(PotionEffectType.WITHER, 140, 0)
        };
    }

    @Override
    public void run() {
        nextEffect--;
        if (nextEffect <= 0) {
            if (this.parent.isDeathmatch()) {
                return;
            }

            Random rnd = new Random();
            nextEffect = 30 + rnd.nextInt(60);

            // On applique un effet dans la liste
            for (UUID player : parent.getDimensionsManager().getPlayersInDimension(DimensionsManager.Dimension.PARALLEL)) {
                int effect = rnd.nextInt(effects.length);
                Player p = Bukkit.getPlayer(player);
                Bukkit.getScheduler().runTask(ParallelsPVP.instance, () -> {
                    if (p != null)
                        p.addPotionEffect(effects[effect]);
                });
            }
        }
    }


}
