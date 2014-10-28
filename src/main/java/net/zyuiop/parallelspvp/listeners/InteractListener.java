package net.zyuiop.parallelspvp.listeners;

import net.zyuiop.parallelspvp.ParallelsPVP;
import net.zyuiop.parallelspvp.arena.ParallelsPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Created by zyuiop on 26/09/14.
 * This isn't actually a Network listener.
 */
public class InteractListener implements Listener {

    protected ParallelsPVP plugin;

    public InteractListener(ParallelsPVP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if (ev.getAction().equals(Action.RIGHT_CLICK_BLOCK) || ev.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            if (ev.getItem() == null)
                return;

            if (ev.getItem().equals(ParallelsPVP.getCompass())) {
                Player p = ev.getPlayer();
                Player nearest = null;
                for (Entity e : p.getNearbyEntities(1000D, 1000D, 1000D)) {
                    if (e instanceof Player) {
                        if (!plugin.getArena().isPlaying(new ParallelsPlayer((Player)e)))
                            continue;

                        if (nearest == null || e.getLocation().distance(p.getLocation()) < e.getLocation().distance(nearest.getLocation())) {
                            nearest = (Player)e;
                        }
                    }
                }
                if (nearest == null)
                    p.sendMessage(ChatColor.RED+"Il n'y a personne dans cette dimension...");
                else {
                    p.sendMessage(ChatColor.GREEN+"Votre boussole pointe désormais vers "+ChatColor.GOLD+nearest.getName());
                    p.setCompassTarget(nearest.getLocation());
                }
            } else if (ev.getItem().equals(ParallelsPVP.getSwap()) && ev.getItem().getAmount() == 1) {
                ev.setCancelled(true);
                plugin.getArena().getDimensionsManager().swap(ev.getPlayer());
            }
        }
    }
}
