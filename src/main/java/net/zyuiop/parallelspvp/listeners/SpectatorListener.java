package net.zyuiop.parallelspvp.listeners;

import net.zyuiop.parallelspvp.ParallelsPVP;
import net.zyuiop.parallelspvp.arena.Arena;
import net.zyuiop.parallelspvp.arena.ParallelsPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by zyuiop on 13/09/14.
 */
public class SpectatorListener implements Listener {

    protected ParallelsPVP plugin;
    protected Arena arena;

    public SpectatorListener(ParallelsPVP plugin) {
        this.plugin = plugin;
        this.arena = plugin.getArena();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public boolean cancel(Player p) {
        if (!arena.isStarted()) {
            return true;
        }
        return !arena.isPlaying(new ParallelsPlayer(p));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        e.setCancelled(cancel(e.getPlayer()));
        if (!e.isCancelled()) {
            boolean canBreak = arena.canBreak(e.getBlock().getType());
            if (!canBreak) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        e.setCancelled(cancel(e.getPlayer()));
        if (!e.isCancelled()) {
            if (e.getBlock().getType() != Material.TNT)
                e.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        e.setCancelled(cancel(e.getPlayer()));
        if (!arena.isPlaying(new ParallelsPlayer(e.getPlayer()))) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (e.getItem().getType() == Material.COMPASS) {
                    arena.tpMenu(e.getPlayer());
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) e.setCancelled(cancel((Player)e.getEntity()));
    }


    @EventHandler
    public void pickup(PlayerPickupItemEvent e) {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void pickup(PlayerDropItemEvent e) {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent ev) {
        ev.blockList().clear();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (arena.isPlaying(new ParallelsPlayer(e.getPlayer())))
            return;

        if (!arena.isStarted())
            return;

        List<Entity> entities = e.getPlayer().getNearbyEntities(1, 1, 1);
        for (Entity ent : entities) {
            if (ent instanceof Player) {
                Player near = (Player) ent;
                if (arena.isPlaying(new ParallelsPlayer(e.getPlayer()))) {
                    e.getPlayer().sendMessage(ChatColor.RED+"Merci de ne pas tourner autour des joueurs !");
                    e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(-1));
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void onBukket(PlayerBucketFillEvent e) {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void onBukket(PlayerBucketEmptyEvent e) {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void onHanging(HangingBreakByEntityEvent e) {
        if (e.getEntity() instanceof Player)
            e.setCancelled(cancel((Player) e.getEntity()));
    }

}
