package net.samagames.dimensions.tasks;

import net.samagames.dimensions.arena.Arena;
import net.samagames.dimensions.arena.DimensionsManager;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;
import java.util.UUID;

/**
 * Created by zyuiop on 26/09/14.
 * Updated by Rigner on 07/08/16.
 */
public class RandomEffects implements Runnable
{

    private Arena parent;
    private int nextEffect = 60;
    private PotionEffect[] effects;

    public RandomEffects(Arena parentArena)
    {
        this.parent = parentArena;
        this.effects = new PotionEffect[]
                {
                    new PotionEffect(PotionEffectType.HARM, 1, 0),
                    new PotionEffect(PotionEffectType.BLINDNESS, 60, 0),
                    new PotionEffect(PotionEffectType.SLOW, 100, 0),
                    new PotionEffect(PotionEffectType.WITHER, 140, 0)
                };
    }

    @Override
    public void run()
    {
        this.nextEffect--;
        if (this.nextEffect <= 0)
        {
            if (this.parent.isDeathmatch())
                return ;

            Random rnd = new Random();
            this.nextEffect = 30 + rnd.nextInt(60);

            // On applique un effet dans la liste
            for (UUID player : this.parent.getDimensionsManager().getPlayersInDimension(DimensionsManager.Dimension.PARALLEL))
            {
                int effect = rnd.nextInt(this.effects.length);
                Player p = this.parent.getPlugin().getServer().getPlayer(player);
                this.parent.getPlugin().getServer().getScheduler().runTask(this.parent.getPlugin(), () ->
                {
                    if (p != null)
                        p.addPotionEffect(this.effects[effect]);
                });
            }
        }
    }


}
