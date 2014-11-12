package net.zyuiop.parallelspvp.tasks;

import net.samagames.gameapi.json.Status;
import net.zyuiop.parallelspvp.ParallelsPVP;
import net.zyuiop.parallelspvp.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Created by zyuiop on 26/09/14.
 */
public class BeginCountdown implements Runnable {

    protected Arena parent;
    protected int maxPlayers = 0;
    protected int minPlayers = 0;
    protected boolean ready = false;
    protected int time = 121; // 2 minutes

    public BeginCountdown(Arena parentArena, int maxPlayers, int minPlayers) {
        this.parent = parentArena;
        this.maxPlayers = maxPlayers;
        this.minPlayers = minPlayers;
    }

    @Override
    public void run() {
        int nPlayers = parent.countPlayers();

        if (nPlayers >= maxPlayers && time > 10) {
            ready = true;
            time = 10;
        } else {
            if (nPlayers >= minPlayers && !ready) {
                ready = true;
                parent.updateStatus(Status.Starting);
                time = 121;
            }

            if (nPlayers >= (maxPlayers/100)*75 && time > 60) {
                time = 61;
            }

            if (nPlayers >= (maxPlayers/100)*85 && time > 30) {
                time = 31;
            }

            if (nPlayers < minPlayers && ready) {
                ready = false;
                Bukkit.broadcastMessage(ParallelsPVP.pluginTAG+ ChatColor.RED+" Il n'y a plus assez de joueurs pour commencer.");
                parent.updateStatus(Status.Available);
                for (Player p : Bukkit.getOnlinePlayers())
                    p.setLevel(120);
            }

            if (ready) {
                time--;
                timeBroadcast();
            }
        }
    }

    public void timeBroadcast() {
        if (time == 0) {
            parent.start();
            return;
        }

        for (Player p : Bukkit.getOnlinePlayers())
            p.setLevel(time);

        if (time <= 5 || time == 10 || time % 30 == 0) {
            Bukkit.broadcastMessage(ParallelsPVP.pluginTAG+ ChatColor.YELLOW+" Début de la partie dans "+ChatColor.RED+time+" seconde"+((time > 1) ? "s" : "")+" !");
        }

        if (time <= 5 || time == 10) {
            parent.broadcastSound(Sound.NOTE_PIANO);
        }
    }
}
