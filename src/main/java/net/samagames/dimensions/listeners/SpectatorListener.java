package net.samagames.dimensions.listeners;

import net.samagames.api.games.Status;
import net.samagames.dimensions.Dimensions;
import net.samagames.dimensions.arena.Arena;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.ArrayList;
import java.util.List;

/*
 * This file is part of Dimensions.
 *
 * Dimensions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dimensions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Dimensions.  If not, see <http://www.gnu.org/licenses/>.
 */
public class SpectatorListener implements Listener
{
    private List<Material> whitelist = new ArrayList<>();
    protected Arena arena;

    public SpectatorListener(Dimensions plugin)
    {
        this.arena = plugin.getArena();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        this.whitelist.add(Material.TNT);
        this.whitelist.add(Material.WORKBENCH);
        this.whitelist.add(Material.FURNACE);
        this.whitelist.add(Material.CAKE);
        this.whitelist.add(Material.CAKE_BLOCK);

    }

    private boolean cancel(Player p)
    {
        return !this.arena.isInGame() || !(this.arena.hasPlayer(p) && !this.arena.isSpectator(p));
    }

    @EventHandler
    public void onMoveEvent(PlayerMoveEvent event)
    {
        if (this.arena.getStatus().equals(Status.IN_GAME) && !this.arena.isInGame() && (this.arena.hasPlayer(event.getPlayer()) && !this.arena.isSpectator(event.getPlayer())))
            if (event.getTo().getBlockX() != event.getFrom().getBlockX() || event.getTo().getBlockZ() != event.getFrom().getBlockZ())
                event.setTo(event.getFrom());
    }

    @EventHandler
    public void onLoseFood(final FoodLevelChangeEvent event)
    {
        event.setCancelled(this.cancel((Player)event.getEntity()));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e)
    {
        e.setCancelled(cancel(e.getPlayer()));
        if (!e.isCancelled())
        {
            boolean canBreak = this.arena.canBreak(e.getBlock().getType());
            if (!canBreak)
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onRain(final WeatherChangeEvent event)
    {
        if (event.toWeatherState())
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e)
    {
        e.setCancelled(cancel(e.getPlayer()));
        if (!e.isCancelled())
        {
            if (e.getBlock().getType() != Material.TNT && e.getBlock().getType() != Material.WORKBENCH && e.getBlock().getType() != Material.FURNACE)
                e.setCancelled(true);
        }
        else
        {
            final int x = e.getBlock().getX();
            final int y = e.getBlock().getY();
            final int z = e.getBlock().getZ();
            final World w = e.getBlock().getWorld();
            final boolean ref = this.whitelist.contains(w.getBlockAt(x, y + 1, z).getType())
                    || this.whitelist.contains(w.getBlockAt(x, y - 1, z).getType())
                    || this.whitelist.contains(w.getBlockAt(x + 1, y, z).getType())
                    || this.whitelist.contains(w.getBlockAt(x - 1, y, z).getType())
                    || this.whitelist.contains(w.getBlockAt(x, y, z + 1).getType())
                    || this.whitelist.contains(w.getBlockAt(x, y, z - 1).getType());
            if (ref)
            {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "Vous ne pouvez pas placer de bloc ici.");
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e)
    {
        e.setCancelled(cancel(e.getPlayer()));
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            if (!(this.arena.isPlaying(e.getPlayer())))
            {
                if (e.getItem() != null && e.getItem().getType() == Material.COMPASS)
                    this.arena.tpMenu(e.getPlayer());
            }
            else if(e.getItem() != null && e.getItem().getType() == Material.WRITTEN_BOOK)
                e.setCancelled(false);
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent e)
    {
        if (e.getEntity() instanceof Player)
            e.setCancelled(cancel((Player)e.getEntity()));
    }


    @EventHandler
    public void pickup(PlayerPickupItemEvent e)
    {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void pickup(PlayerDropItemEvent e)
    {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent ev)
    {
        ev.blockList().clear();
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e)
    {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void onBukket(PlayerBucketFillEvent e)
    {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void onBukket(PlayerBucketEmptyEvent e)
    {
        e.setCancelled(cancel(e.getPlayer()));
    }

    @EventHandler
    public void onHanging(HangingBreakByEntityEvent e)
    {
        if (e.getEntity() instanceof Player)
            e.setCancelled(cancel((Player) e.getEntity()));
    }

}
