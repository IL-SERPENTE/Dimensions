package net.samagames.parallelspvp.tasks;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.Status;
import net.samagames.parallelspvp.arena.Arena;
import net.samagames.tools.Titles;
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
        int nPlayers = parent.getConnectedPlayers();

        if (nPlayers >= maxPlayers && time > 10) {
            ready = true;
            time = 10;
        } else {
            if (nPlayers >= minPlayers && !ready) {
                ready = true;
                parent.setStatus(Status.STARTING);
                time = 121;
            }

            if (nPlayers >= ((double)maxPlayers/100.0)*65.0 && time > 60) {
                time = 61;
            }

            if (nPlayers >= ((double)maxPlayers/100.0)*85.0 && time > 30) {
                time = 31;
            }

            if (nPlayers < minPlayers && ready) {
                ready = false;
                SamaGamesAPI.get().getGameManager().getCoherenceMachine().getMessageManager().writeCustomMessage(ChatColor.RED+"Il n'y a plus assez de joueurs pour commencer.", true);
                parent.setStatus(Status.WAITING_FOR_PLAYERS);
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
            parent.startGame();
            return;
        }

        for (Player p : Bukkit.getOnlinePlayers()){
            p.setLevel(time);
            if (this.time <= 5 || this.time == 10) {
                Titles.sendTitle(p, 2, 16, 2, ChatColor.GOLD + "Début dans " + ChatColor.RED + this.time + ChatColor.GOLD + " sec", ChatColor.GOLD + "Préparez vous au combat !");
            }
        }

        if (time <= 5 || time == 10 || time % 30 == 0) {
            SamaGamesAPI.get().getGameManager().getCoherenceMachine().getMessageManager().writeCustomMessage(ChatColor.YELLOW + "Début de la partie dans " + ChatColor.RED + time + " seconde" + ((time > 1) ? "s" : "") + " !", true);
        }

        if (time <= 5 || time == 10) {
            parent.broadcastSound(Sound.NOTE_PIANO);
        }
    }
}
