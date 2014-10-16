package net.zyuiop.parallelspvp.listeners;

import net.samagames.network.client.GamePlayer;
import net.zyuiop.coinsManager.CoinsManager;
import net.zyuiop.parallelspvp.ParallelsPVP;
import net.zyuiop.parallelspvp.arena.Arena;
import net.zyuiop.parallelspvp.arena.DimensionsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

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
        Arena arena = plugin.getArena();
        if (!arena.isPlaying(new GamePlayer((Player) event.getEntity()))) {
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
                        int montant = CoinsManager.syncCreditJoueur(event.getEntity().getUniqueId(), 2, false, true);
                        ((Player)obj.getDamager()).sendMessage(ChatColor.GOLD + "Vous gagnez " + montant + " coins " + ChatColor.AQUA + "(Un joueur tué !)");
                    }
                });
            }
        }

        arena.stumpPlayer(event.getEntity().getUniqueId(), false);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent ev) {
        if (ev.getCause() == EntityDamageEvent.DamageCause.WITHER)
            ev.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getDamager() instanceof Player) {
                Arena arena = plugin.getArena();
                if (!arena.isPlaying(new GamePlayer((Player) event.getDamager())) || !arena.isPlaying(new GamePlayer((Player) event.getEntity())) || !arena.isPVPEnabled()) {
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
            if (arena.isPlaying(new GamePlayer((Player) event.getEntity()))) {
                if (arena.getDimensionsManager().getDimension((Player) event.getEntity()).equals(DimensionsManager.Dimension.PARALLEL))
                    event.setCancelled(event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED));
            }
        }
    }
}
