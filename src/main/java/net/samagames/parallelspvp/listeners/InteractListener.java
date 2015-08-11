package net.samagames.parallelspvp.listeners;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.themachine.ICoherenceMachine;
import net.samagames.parallelspvp.ParallelsPVP;
import net.samagames.parallelspvp.arena.DimensionsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by zyuiop on 26/09/14.
 * This isn't actually a Network listener.
 */
public class InteractListener implements Listener {

    protected ParallelsPVP plugin;
    protected HashMap<UUID, BukkitTask> tasks = new HashMap<>();

    protected ICoherenceMachine coherenceMachine;

    public InteractListener(ParallelsPVP plugin) {
        this.plugin = plugin;

        coherenceMachine = SamaGamesAPI.get().getGameManager().getCoherenceMachine();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if (ev.getClickedBlock() != null && (ev.getClickedBlock().getType() == Material.BED || ev.getClickedBlock().getType() == Material.BED_BLOCK)) {
            ev.setCancelled(true);
        }

        if (ev.getAction().equals(Action.RIGHT_CLICK_BLOCK) || ev.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            if (ev.getItem() == null)
                return;

            if (ev.getItem().equals(ParallelsPVP.getCompass())) {
                final Player p = ev.getPlayer();
                Player nearest = null;
                for (Entity e : p.getNearbyEntities(1000D, 1000D, 1000D)) {
                    if (e instanceof Player) {
                        Player current = (Player) e;
                        if (!plugin.getArena().isPlaying(current))
                            continue;

                        if (plugin.getArena().getDimensionsManager().getDimension(current) != plugin.getArena().getDimensionsManager().getDimension(p)) {
                            continue;
                        }

                        if (nearest == null || e.getLocation().distance(p.getLocation()) < e.getLocation().distance(nearest.getLocation())) {
                            nearest = current;
                        }
                    }
                }
                if (nearest == null)
                    p.sendMessage(ChatColor.RED+"Il n'y a personne dans cette dimension...");
                else {
                    p.sendMessage(ChatColor.GREEN + "Votre boussole pointe désormais vers " + ChatColor.GOLD + nearest.getName());
                    p.setCompassTarget(nearest.getLocation());
                    targetPlayer(p, nearest);
                }
            } else if (ev.getItem().equals(ParallelsPVP.getSwap()) && ev.getItem().getAmount() == 1) {
                ev.setCancelled(true);
                plugin.getArena().getDimensionsManager().swap(ev.getPlayer());
            }
        } else if (ev.getAction().equals(Action.LEFT_CLICK_BLOCK) || ev.getAction().equals(Action.LEFT_CLICK_AIR)) {
            if (ev.getItem() == null)
                return;

            if (!ev.getItem().equals(ParallelsPVP.getCompass()))
                return;

            UUID target = plugin.getArena().getTarget(ev.getPlayer().getUniqueId());
            if (target == null)
                return;

            Player p = Bukkit.getPlayer(target);
            if ((p == null || !p.isOnline()) && plugin.getArena().isPVPEnabled()) {
                Player t = plugin.getArena().getNewTarget(ev.getPlayer().getUniqueId());
                ParallelsPVP.interactListener.targetPlayer(ev.getPlayer(), t);
                ev.getPlayer().sendMessage(coherenceMachine.getGameTag() + ChatColor.GOLD+"Votre cible est "+t.getDisplayName()+ChatColor.GOLD+". Tuez le pour gagner un bonus de coins !");
                ev.getPlayer().sendMessage(coherenceMachine.getGameTag() + ChatColor.GOLD+"Votre boussole pointe désormais vers ce joueur. Faites clic gauche avec votre boussole pour la pointer vers lui à nouveau !");
            }else if (this.plugin.getArena().isPVPEnabled()) {
                final DimensionsManager dm = this.plugin.getArena().getDimensionsManager();
                if (dm.getDimension(p) == dm.getDimension(ev.getPlayer())) {
                    ev.getPlayer().sendMessage(ChatColor.GREEN + "Votre boussole pointe d\u00e9sormais vers " + ChatColor.GOLD + p.getDisplayName());
                    ev.getPlayer().sendMessage(ChatColor.GREEN + "Tuez ce joueur pour gagner un bonus de coins !");
                    this.targetPlayer(ev.getPlayer(), p);
                }
                else {
                    ev.getPlayer().sendMessage(coherenceMachine.getGameTag() + p.getDisplayName() + ChatColor.RED + " se situe dans une autre dimension...");
                }
            }
        }
    }

    public void unregisterTask(Player player) {
        BukkitTask current = tasks.get(player.getUniqueId());
        if (current != null)
            current.cancel();
        else
            plugin.getLogger().warning("Unregistering task but no task detected...");
        tasks.remove(player.getUniqueId());
        plugin.getLogger().info("Unregistered task for player " + player.getDisplayName());
    }

    public void targetPlayer(final Player player, final Player target) {
        BukkitTask sched = Bukkit.getScheduler().runTaskTimer(ParallelsPVP.instance, () -> {
            if (target.isOnline()) {
                if (plugin.getArena().getDimensionsManager().getDimension(player) != plugin.getArena().getDimensionsManager().getDimension(target)) {
                    player.sendMessage(coherenceMachine.getGameTag() + " " + target.getDisplayName() + ChatColor.RED + " se situe dans une autre dimension...");
                    unregisterTask(player);
                } else
                    player.setCompassTarget(target.getLocation());
            } else
                unregisterTask(player);
        }, 5L, 5L);
        updateTask(player, sched);
    }

    public void updateTask(Player player, BukkitTask task) {
        unregisterTask(player);
        tasks.put(player.getUniqueId(), task);
    }
}
