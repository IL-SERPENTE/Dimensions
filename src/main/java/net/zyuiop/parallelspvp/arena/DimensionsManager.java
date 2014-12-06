package net.zyuiop.parallelspvp.arena;

import net.zyuiop.parallelspvp.ParallelsPVP;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by zyuiop on 26/09/14.
 */
public class DimensionsManager {

    public enum Dimension {
        OVERWORLD, PARALLEL;
    }

    protected HashMap<UUID, Dimension> dimensions = new HashMap<UUID, Dimension>();
    protected Arena parentArena;
    protected int decalage;
    protected String overworldName;
    protected String hardName;
    protected HashMap<UUID, Integer> waitList = new HashMap<>();
    protected HashMap<UUID, BukkitTask> tasks = new HashMap<>();

    public DimensionsManager(Arena parentArena, int decalage, String overworldName, String hardName) {
        this.parentArena = parentArena;
        this.decalage = decalage;
        this.overworldName = overworldName;
        this.hardName = hardName;
    }

    public ArrayList<UUID> getPlayersInDimension(Dimension dim) {
        ArrayList<UUID> ret = new ArrayList<>();
        for (UUID pl : dimensions.keySet()) {
            if (dimensions.get(pl) == Dimension.PARALLEL)
                ret.add(pl);
        }
        return ret;
    }

    public void swap(Player p) {
        if (!parentArena.isStarted() || parentArena.isDeathmatch)
            return;

        if (waitList.containsKey(p.getUniqueId())) {
            p.sendMessage(ChatColor.RED+"Merci d'attendre "+waitList.get(p.getUniqueId())+" seconde(s) avant de changer de dimension.");
            return;
        }

        ParallelsPlayer ap = new ParallelsPlayer(p);
        Dimension dim = dimensions.get(ap.getPlayerID());
        if (dim == null)
            dim = Dimension.OVERWORLD;

        Location tpTo = new Location(p.getLocation().getWorld(), p.getLocation().getBlockX()+0.5, p.getLocation().getBlockY(), p.getLocation().getBlockZ()+0.5, p.getLocation().getYaw(), p.getLocation().getPitch());
        Location tpToWork = new Location(p.getLocation().getWorld(), p.getLocation().getBlockX()+0.5, p.getLocation().getBlockY() + 1, p.getLocation().getBlockZ()+0.5, p.getLocation().getYaw(), p.getLocation().getPitch());
        if (dim == Dimension.OVERWORLD) {
            tpTo.setX(tpTo.getX() - decalage);
            dim = Dimension.PARALLEL;
            p.setPlayerTime(17000,false);
        } else {
            tpTo.setX(tpTo.getX() + decalage);
            dim = Dimension.OVERWORLD;
            p.setPlayerTime(6000, false);
        }

        Block b = tpTo.getBlock();
        Block up = tpToWork.getBlock();
        if (b.isEmpty() && up.isEmpty()) {
            tpToWork.setY(tpToWork.getY() - 2);
            while (tpToWork.getBlock().isEmpty()) {
                tpToWork.setY(tpToWork.getY()-1);
                if (tpToWork.getY() < 2) {
                    p.sendMessage(ChatColor.RED+"On dirait que vous ne pouvez pas changer de dimension ici...");
                    return;
                }
            }
            tpTo.setY(tpToWork.getY()+1);
            p.teleport(tpTo);

            p.playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 0));
            dimensions.put(ap.getPlayerID(), dim);

            startCountdown(p);

            // Effets swag //
            if (dim == Dimension.OVERWORLD)
                p.sendMessage(ChatColor.DARK_GREEN+"Vous êtes maintenant dans la dimension "+ChatColor.GREEN+overworldName);
            else
                p.sendMessage(ChatColor.DARK_RED+"Vous êtes maintenant dans la dimension "+ChatColor.RED+hardName);

        } else {
            p.sendMessage(ChatColor.RED + "On dirait que vous ne pouvez pas changer de dimension ici...");
        }
    }

    public Dimension getDimension(Player player) {
        if (parentArena.isDeathmatch)
            return Dimension.OVERWORLD;

        Dimension ret = dimensions.get(player.getUniqueId());
        return (ret == null) ? Dimension.OVERWORLD : ret;
    }

    public void startCountdown(final Player player) {
        waitList.put(player.getUniqueId(), 5);
        this.tasks.put(player.getUniqueId(), Bukkit.getScheduler().runTaskTimer(ParallelsPVP.instance, new Runnable() {
            @Override
            public void run() {
                Integer i = waitList.get(player.getUniqueId());
                if (i > 0)
                    waitList.put(player.getUniqueId(), i - 1);
                else {
                    waitList.remove(player.getUniqueId());
                    cancel(player.getUniqueId());
                }
            }
        }, 20L, 20L));
    }

    public void cancel(UUID player) {
        BukkitTask t = tasks.get(player);
        waitList.remove(player);
        if (t != null) {
            tasks.remove(player);
            t.cancel();
        }
    }
}
