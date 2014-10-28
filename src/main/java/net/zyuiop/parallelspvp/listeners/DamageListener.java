package net.zyuiop.parallelspvp.listeners;

import net.samagames.network.client.GamePlayer;
import net.zyuiop.coinsManager.CoinsManager;
import net.zyuiop.parallelspvp.ParallelsPVP;
import net.zyuiop.parallelspvp.arena.Arena;
import net.zyuiop.parallelspvp.arena.DimensionsManager;
import net.zyuiop.parallelspvp.arena.ParallelsPlayer;
import net.zyuiop.statsapi.StatsApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Created by zyuiop on 26/09/14.
 */
public class DamageListener implements Listener {

    protected ParallelsPVP plugin;

    public DamageListener(ParallelsPVP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(final PlayerDeathEvent event) {
        final Arena arena = plugin.getArena();
        if (!arena.isPlaying(new ParallelsPlayer((Player) event.getEntity()))) {
            return;
        }

        EntityDamageEvent last = event.getEntity().getLastDamageCause();
        if (last instanceof EntityDamageByEntityEvent) {
            final EntityDamageByEntityEvent obj = (EntityDamageByEntityEvent)last;
            if (obj.getDamager() instanceof Player) {
                // C'est lui ki gagn des coins
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                       CoinsManager.creditJoueur(obj.getDamager().getUniqueId(), 2, true, true, "Un joueur tué !");
                        StatsApi.increaseStat(obj.getDamager().getUniqueId(), "parallelspvp", "kills", 1);
                        //((Player)obj.getDamager()).sendMessage(ChatColor.GOLD + "Vous gagnez " + montant + " coins " + ChatColor.AQUA + "(Un joueur tué !)");
                    }
                });
            }
        }


        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
           arena.stumpPlayer(event.getEntity().getUniqueId(), false);
            }
        }, 10L);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent ev) {
        if (ev.getCause() == EntityDamageEvent.DamageCause.WITHER)
            ev.setCancelled(true);

        if (!plugin.getArena().isPVPEnabled()) {
            if (ev.getCause() == EntityDamageEvent.DamageCause.POISON || ev.getCause() == EntityDamageEvent.DamageCause.MAGIC)
                ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent ev) {
        ev.setRespawnLocation(plugin.getArena().getWaitLocation());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getDamager() instanceof Player) {
                Arena arena = plugin.getArena();
                if (!arena.isPlaying(new ParallelsPlayer((Player) event.getDamager())) || !arena.isPlaying(new ParallelsPlayer((Player) event.getEntity())) || !arena.isPVPEnabled()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            Arena arena = plugin.getArena();
            if (arena.isPlaying(new ParallelsPlayer((Player) event.getEntity()))) {
                if (arena.getDimensionsManager().getDimension((Player) event.getEntity()).equals(DimensionsManager.Dimension.PARALLEL))
                    event.setCancelled(event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED));
            }
        }
    }
}
