package net.samagames.dimensions.listeners;

import net.samagames.api.games.themachine.ICoherenceMachine;
import net.samagames.dimensions.Dimensions;
import net.samagames.dimensions.arena.DimensionsManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by zyuiop on 26/09/14.
 * This isn't actually a Network listener.
 */
public class InteractListener implements Listener
{
    private Dimensions plugin;
    private Map<UUID, BukkitTask> tasks = new HashMap<>();

    private ICoherenceMachine coherenceMachine;

    public InteractListener(Dimensions plugin)
    {
        this.plugin = plugin;

        this.coherenceMachine = plugin.getArena().getCoherenceMachine();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev)
    {
        if (ev.getClickedBlock() != null && (ev.getClickedBlock().getType() == Material.BED || ev.getClickedBlock().getType() == Material.BED_BLOCK))
            ev.setCancelled(true);

        if (ev.getAction().equals(Action.RIGHT_CLICK_BLOCK) || ev.getAction().equals(Action.RIGHT_CLICK_AIR))
        {
            if (ev.getItem() == null)
                return ;

            if (ev.getItem().equals(Dimensions.getCompass()))
            {
                final Player p = ev.getPlayer();
                Player nearest = null;
                for (Entity e : p.getNearbyEntities(1000D, 1000D, 1000D))
                {
                    if (e instanceof Player)
                    {
                        Player current = (Player) e;
                        if (!this.plugin.getArena().isPlaying(current))
                            continue ;

                        if (this.plugin.getArena().getDimensionsManager().getDimension(current) != this.plugin.getArena().getDimensionsManager().getDimension(p))
                            continue ;
                        
                        if (p.equals(current))
                        	continue ;
                        
                        if (nearest == null || e.getLocation().distance(p.getLocation()) < e.getLocation().distance(nearest.getLocation()))
                            nearest = current;
                    }
                }
                if (nearest == null)
                    p.sendMessage(ChatColor.RED + "Il n'y a personne dans cette dimension...");
                else
                {
                    p.sendMessage(ChatColor.GREEN + "Votre boussole pointe désormais vers " + ChatColor.GOLD + nearest.getName());
                    p.setCompassTarget(nearest.getLocation());
                    targetPlayer(p, nearest);
                }
            }
            else if (ev.getItem().equals(Dimensions.getSwap()) && ev.getItem().getAmount() == 1)
            {
                ev.setCancelled(true);
                this.plugin.getArena().getDimensionsManager().swap(ev.getPlayer());
            }
            if (!this.plugin.getArena().isInGame() && ev.getItem().getType() != Material.WRITTEN_BOOK)
                ev.setCancelled(true);
        }
        else if (ev.getAction().equals(Action.LEFT_CLICK_BLOCK) || ev.getAction().equals(Action.LEFT_CLICK_AIR))
        {
            if (ev.getItem() == null)
                return ;

            if (!ev.getItem().equals(Dimensions.getCompass()))
                return ;

            UUID target = this.plugin.getArena().getTarget(ev.getPlayer().getUniqueId());
            if (target == null)
                return ;

            Player p = this.plugin.getServer().getPlayer(target);
            if ((p == null || !p.isOnline()) && this.plugin.getArena().isPVPEnabled())
            {
                Player t = this.plugin.getArena().getNewTarget(ev.getPlayer().getUniqueId());
                targetPlayer(ev.getPlayer(), t);
                ev.getPlayer().sendMessage(ChatColor.GOLD + "Votre cible est " + t.getDisplayName() + ChatColor.GOLD + ". Tuez le pour gagner un bonus de coins !");
                ev.getPlayer().sendMessage(ChatColor.GOLD + "Votre boussole pointe désormais vers ce joueur. Faites clic gauche avec votre boussole pour la pointer vers lui à nouveau !");
            }
            else if (p != null && this.plugin.getArena().isPVPEnabled())
            {
                final DimensionsManager dm = this.plugin.getArena().getDimensionsManager();
                if (dm.getDimension(p) == dm.getDimension(ev.getPlayer()))
                {
                    ev.getPlayer().sendMessage(ChatColor.GREEN + " Votre boussole pointe désormais vers " + ChatColor.GOLD + p.getDisplayName());
                    ev.getPlayer().sendMessage(ChatColor.GREEN + " Tuez ce joueur pour gagner un bonus de coins !");
                    this.targetPlayer(ev.getPlayer(), p);
                }
                else
                    ev.getPlayer().sendMessage(this.coherenceMachine.getGameTag() + " " + p.getDisplayName() + ChatColor.RED + " se situe dans une autre dimension...");
            }
        }
    }

    public void unregisterTask(Player player)
    {
        BukkitTask current = this.tasks.get(player.getUniqueId());
        if (current != null)
            current.cancel();
        else
            this.plugin.getLogger().warning("Unregistering task but no task detected...");
        this.tasks.remove(player.getUniqueId());
        this.plugin.getLogger().info("Unregistered task for player " + player.getDisplayName());
    }

    public void targetPlayer(final Player player, final Player target)
    {
        BukkitTask sched = this.plugin.getServer().getScheduler().runTaskTimer(Dimensions.instance, () ->
        {
            if (target != null && target.isOnline())
            {
                if (this.plugin.getArena().getDimensionsManager().getDimension(player) != this.plugin.getArena().getDimensionsManager().getDimension(target))
                {
                    player.sendMessage(this.coherenceMachine.getGameTag() + " " + target.getDisplayName() + ChatColor.RED + " se situe dans une autre dimension...");
                    unregisterTask(player);
                }
                else
                    player.setCompassTarget(target.getLocation());
            }
            else
                unregisterTask(player);
        }, 5L, 5L);
        updateTask(player, sched);
    }

    private void updateTask(Player player, BukkitTask task)
    {
        unregisterTask(player);
        this.tasks.put(player.getUniqueId(), task);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onUnloadChunk(WorldUnloadEvent event)
    {
        event.setCancelled(true);
    }
}