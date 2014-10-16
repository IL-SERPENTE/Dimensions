package net.zyuiop.parallelspvp.arena;

import net.samagames.network.client.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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

        GamePlayer ap = new GamePlayer(p);
        Dimension dim = dimensions.get(ap.getPlayerID());
        if (dim == null)
            dim = Dimension.OVERWORLD;

        Location tpTo = new Location(p.getLocation().getWorld(), p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), p.getLocation().getYaw(), p.getLocation().getPitch());
        Location tpToWork = new Location(p.getLocation().getWorld(), p.getLocation().getX(), p.getLocation().getY() + 1, p.getLocation().getZ(), p.getLocation().getYaw(), p.getLocation().getPitch());
        if (dim == Dimension.OVERWORLD) {
            tpTo.setX(tpTo.getX() - decalage);
            dim = Dimension.PARALLEL;
        } else {
            tpTo.setX(tpTo.getX() + decalage);
            dim = Dimension.OVERWORLD;
        }

        Block b = tpTo.getBlock();
        Block up = tpToWork.getBlock();
        if (b.isEmpty() && up.isEmpty()) {
            p.teleport(tpTo);
            tpToWork.setY(tpToWork.getY() - 2);
            while (tpToWork.getBlock().isEmpty()) {
                tpToWork.setY(tpToWork.getY()-1);
                if (tpToWork.getY() < 2) {
                    p.sendMessage(ChatColor.RED+"On dirait que vous ne pouvez pas changer de dimension ici...");
                    return;
                }
            }
            tpTo.setY(tpToWork.getY()+1);

            p.playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 0));
            dimensions.put(ap.getPlayerID(), dim);

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
}
