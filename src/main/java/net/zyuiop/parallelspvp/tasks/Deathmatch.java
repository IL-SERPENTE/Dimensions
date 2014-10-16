package net.zyuiop.parallelspvp.tasks;

import net.zyuiop.parallelspvp.ParallelsPVP;
import net.zyuiop.parallelspvp.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by zyuiop on 26/09/14.
 */
public class Deathmatch implements Runnable {

    protected Arena parent;
    protected int time = 61; // 1 minute

    public Deathmatch(Arena parentArena) {
        this.parent = parentArena;
    }

    @Override
    public void run() {
        time--;
        timeBroadcast();
    }

    public void timeBroadcast() {
        if (time <= 0) {
            parent.startDeathMatch();
            if (!parent.isPVPEnabled())
                parent.enablePVP();
            return;
        }

        if (time <= 5 || time == 10 || time == 30 || time == 60) {
            Bukkit.broadcastMessage(ParallelsPVP.pluginTAG + ChatColor.DARK_RED + " Deathmatch dans " + ChatColor.RED+time+" seconde" + ((time > 1) ? "s" : "") + " !");
        }
    }
}
