package net.zyuiop.parallelspvp.tasks;

import net.samagames.gameapi.json.Status;
import net.zyuiop.parallelspvp.ParallelsPVP;
import net.zyuiop.parallelspvp.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Created by zyuiop on 26/09/14.
 */
public class PreparingCountdown implements Runnable, Listener {

    protected Arena parent;
    protected int time = 16; // 15 secondes

    public PreparingCountdown(Arena parentArena) {
        this.parent = parentArena;
    }

    @Override
    public void run() {
        time --;
        timeBroadcast();
    }

    public void timeBroadcast() {
        if (time == 0) {
            parent.start();
            return;
        }

        for (Player p : Bukkit.getOnlinePlayers())
            p.setLevel(time);

        if (time <= 5 || time == 10 || time == 15) {
            Bukkit.broadcastMessage(ParallelsPVP.pluginTAG+ ChatColor.YELLOW+" Démarrage du jeu dans "+ChatColor.RED+time+" seconde"+((time > 1) ? "s" : "")+" !");
        }

        if (time <= 5 || time == 10) {
            parent.broadcastSound(Sound.NOTE_PIANO);
        }
    }
}
