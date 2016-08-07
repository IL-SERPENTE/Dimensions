package net.samagames.dimensions.arena;

import net.samagames.api.games.Status;
import net.samagames.dimensions.Dimensions;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by zyuiop on 26/09/14.
 * Updated by Rigner on 07/08/16.
 */
public class DimensionsManager
{

    protected Map<UUID, Dimension> dimensions = new HashMap<>();
    private Arena parentArena;
    private int offset;
    String overworldName;
    String hardName;
    private Map<UUID, Integer> waitList = new HashMap<>();
    private Map<UUID, BukkitTask> tasks = new HashMap<>();

    DimensionsManager(Arena parentArena, int offset, String overworldName, String hardName)
    {
        this.parentArena = parentArena;
        this.offset = offset;
        this.overworldName = overworldName;
        this.hardName = hardName;
    }

    public List<UUID> getPlayersInDimension(Dimension dim)
    {
        return this.dimensions.keySet().stream().filter(pl -> this.dimensions.get(pl) == dim).collect(Collectors.toList());
    }

    public void swap(Player p)
    {
        if (!this.parentArena.getStatus().equals(Status.IN_GAME) || parentArena.isDeathmatch)
            return ;

        if (this.waitList.containsKey(p.getUniqueId()))
        {
            p.sendMessage(ChatColor.RED+"Merci d'attendre " + this.waitList.get(p.getUniqueId()) + " seconde(s) avant de changer de dimension.");
            return ;
        }

        APlayer aPlayer = this.parentArena.getPlayer(p.getUniqueId());
        Dimension dim = this.dimensions.get(p.getUniqueId());
        if (dim == null)
            dim = Dimension.OVERWORLD;

        final Location oldLoc = new Location(p.getLocation().getWorld(), p.getLocation().getX(), p.getLocation().getY() - 1.0, p.getLocation().getZ());
        final Location tpTo = new Location(p.getLocation().getWorld(), p.getLocation().getBlockX() + 0.5, (double)p.getLocation().getBlockY(), p.getLocation().getBlockZ() + 0.5, p.getLocation().getYaw(), p.getLocation().getPitch());
        final Location tpToWork = new Location(p.getLocation().getWorld(), p.getLocation().getBlockX() + 0.5, (double)(p.getLocation().getBlockY() + 1), p.getLocation().getBlockZ() + 0.5, p.getLocation().getYaw(), p.getLocation().getPitch());
        if (dim == Dimension.OVERWORLD)
        {
            tpTo.setX(tpTo.getX() - this.offset);
            tpToWork.setX(tpToWork.getX() - this.offset);
            dim = Dimension.PARALLEL;
        }
        else
        {
            tpTo.setX(tpTo.getX() + this.offset);
            tpToWork.setX(tpToWork.getX() + this.offset);
            dim = Dimension.OVERWORLD;
        }

        final Block b = tpTo.getBlock();
        final Block up = tpToWork.getBlock();
        if (this.isEmpty(b) && this.isEmpty(up))
        {
            tpToWork.setY(tpToWork.getY() - 1.0);
            while (tpToWork.getBlock().isEmpty())
            {
                tpToWork.setY(tpToWork.getY() - 1.0);
                if (tpToWork.getY() < 2.0)
                {
                    p.sendMessage(ChatColor.RED + "On dirait que vous ne pouvez pas changer de dimension ici...");
                    return ;
                }
            }
            if (dim == Dimension.OVERWORLD)
                p.setPlayerTime(6000L, false);
            else
                p.setPlayerTime(17000L, false);
            tpTo.setY(tpToWork.getY() + 1.0);
            p.teleport(tpTo);
            oldLoc.setY(tpToWork.getY());
            this.swapBlocks(oldLoc, tpToWork);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1.0f, 1.0f);
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 0));
            this.dimensions.put(p.getUniqueId(), dim);
            this.startCountdown(aPlayer);

            // Effets swag //
            if (dim == Dimension.OVERWORLD)
                p.sendMessage(ChatColor.DARK_GREEN + "Vous êtes maintenant dans la dimension " + ChatColor.GREEN + this.overworldName);
            else
                p.sendMessage(ChatColor.DARK_RED + "Vous êtes maintenant dans la dimension " + ChatColor.RED + this.hardName);

        }
        else
            p.sendMessage(ChatColor.RED + "On dirait que vous ne pouvez pas changer de dimension ici...");
    }

    @SuppressWarnings("deprecation")
    private void swapBlocks(final Location loc1, final Location loc2)
    {
        final Location[] locations1 = { loc1, new Location(loc1.getWorld(), loc1.getX(), loc1.getY(), loc1.getZ() - 2.0), new Location(loc1.getWorld(), loc1.getX(), loc1.getY(), loc1.getZ() - 1.0), new Location(loc1.getWorld(), loc1.getX(), loc1.getY(), loc1.getZ() + 1.0), new Location(loc1.getWorld(), loc1.getX(), loc1.getY(), loc1.getZ() + 2.0), new Location(loc1.getWorld(), loc1.getX() - 2.0, loc1.getY(), loc1.getZ()), new Location(loc1.getWorld(), loc1.getX() - 1.0, loc1.getY(), loc1.getZ()), new Location(loc1.getWorld(), loc1.getX() + 1.0, loc1.getY(), loc1.getZ()), new Location(loc1.getWorld(), loc1.getX() + 2.0, loc1.getY(), loc1.getZ()), new Location(loc1.getWorld(), loc1.getX() + 1.0, loc1.getY(), loc1.getZ() + 1.0), new Location(loc1.getWorld(), loc1.getX() + 1.0, loc1.getY(), loc1.getZ() - 1.0), new Location(loc1.getWorld(), loc1.getX() - 1.0, loc1.getY(), loc1.getZ() - 1.0), new Location(loc1.getWorld(), loc1.getX() - 1.0, loc1.getY(), loc1.getZ() + 1.0) };
        final Location[] locations2 = { loc2, new Location(loc2.getWorld(), loc2.getX(), loc2.getY(), loc2.getZ() - 2.0), new Location(loc2.getWorld(), loc2.getX(), loc2.getY(), loc2.getZ() - 1.0), new Location(loc2.getWorld(), loc2.getX(), loc2.getY(), loc2.getZ() + 1.0), new Location(loc2.getWorld(), loc2.getX(), loc2.getY(), loc2.getZ() + 2.0), new Location(loc2.getWorld(), loc2.getX() - 2.0, loc2.getY(), loc2.getZ()), new Location(loc2.getWorld(), loc2.getX() - 1.0, loc2.getY(), loc2.getZ()), new Location(loc2.getWorld(), loc2.getX() + 1.0, loc2.getY(), loc2.getZ()), new Location(loc2.getWorld(), loc2.getX() + 2.0, loc2.getY(), loc2.getZ()), new Location(loc2.getWorld(), loc2.getX() + 1.0, loc2.getY(), loc2.getZ() + 1.0), new Location(loc2.getWorld(), loc2.getX() + 1.0, loc2.getY(), loc2.getZ() - 1.0), new Location(loc2.getWorld(), loc2.getX() - 1.0, loc2.getY(), loc2.getZ() - 1.0), new Location(loc2.getWorld(), loc2.getX() - 1.0, loc2.getY(), loc2.getZ() + 1.0) };
        final List<Location> loc2list = Arrays.asList(locations2);
        final Iterator<Location> iter = loc2list.iterator();
        for (final Location l : locations1)
        {
            final Block block1 = l.getBlock();
            final Block block2 = iter.next().getBlock();
            final Material trans = block1.getType();
            final byte data = block1.getData();
            block1.setType(block2.getType());
            block1.setData(block2.getData());
            block2.setType(trans);
            block2.setData(data);
        }
        loc1.getWorld().createExplosion((double)loc1.getBlockX(), (double)loc1.getBlockY(), (double)loc1.getBlockZ(), 1.0f, true, false);
        loc2.getWorld().createExplosion((double)loc2.getBlockX(), (double)loc2.getBlockY(), (double)loc2.getBlockZ(), 1.0f, false, false);
    }

    private boolean isEmpty(final Block block)
    {
        return block.isEmpty() || block.getType() == Material.CARPET || block.getType() == Material.SIGN || block.getType() == Material.LADDER || block.getType() == Material.SIGN_POST;
    }

    public Dimension getDimension(Player player)
    {
        if (this.parentArena.isDeathmatch)
            return Dimension.OVERWORLD;

        Dimension ret = this.dimensions.get(player.getUniqueId());
        return (ret == null) ? Dimension.OVERWORLD : ret;
    }

    private void startCountdown(final APlayer player)
    {
        this.waitList.put(player.getUUID(), (int) player.getTpTime());
        this.tasks.put(player.getUUID(), this.parentArena.getPlugin().getServer().getScheduler().runTaskTimer(Dimensions.instance, () ->
        {
            Integer i = this.waitList.get(player.getUUID());
            if (i > 0)
                this.waitList.put(player.getUUID(), i - 1);
            else
            {
                this.waitList.remove(player.getUUID());
                cancel(player.getUUID());
            }
        }, 20L, 20L));
    }

    private void cancel(UUID player)
    {
        BukkitTask t = this.tasks.get(player);
        this.waitList.remove(player);
        if (t != null)
        {
            this.tasks.remove(player);
            t.cancel();
        }
    }

    public enum Dimension
    {
        OVERWORLD, PARALLEL
    }
}
