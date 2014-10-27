package net.zyuiop.parallelspvp.tasks;

import net.zyuiop.parallelspvp.ParallelsPVP;
import net.zyuiop.parallelspvp.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

/**
 * Created by zyuiop on 26/09/14.
 */
public class PVPEnable implements Runnable {

    protected Arena parent;
    protected int time = 181; // 3 Minutes

    public PVPEnable(Arena parentArena) {
        this.parent = parentArena;
    }

    @Override
    public void run() {
        time--;
        timeBroadcast();
    }

    public void timeBroadcast() {
        if (time <= 0) {
            parent.enablePVP();
            return;
        }

        if (time <= 5 || time == 10 || time == 30 || time == 60) {
            Bukkit.broadcastMessage(ParallelsPVP.pluginTAG + ChatColor.GOLD + "Le PVP sera activé dans "+time+" secondes !");
        }

        if (time <= 5 || time == 10) {
            parent.broadcastSound(Sound.ARROW_HIT);
        }
    }
}
