package net.samagames.dimensions.tasks;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.themachine.ICoherenceMachine;
import net.samagames.dimensions.Dimensions;
import net.samagames.dimensions.arena.Arena;
import net.samagames.tools.Titles;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * Created by zyuiop on 26/09/14.
 */
public class PreparingCountdown implements Runnable, Listener {

    protected Arena parent;
    protected int time = 16; // 15 secondes

    protected ICoherenceMachine coherenceMachine;

    public PreparingCountdown(Arena parentArena) {
        this.parent = parentArena;

        coherenceMachine = SamaGamesAPI.get().getGameManager().getCoherenceMachine();
    }

    @Override
    public void run() {
        time --;
        timeBroadcast();
    }

    public void timeBroadcast() {
        if (time == 0) {
            Bukkit.getScheduler().runTask(Dimensions.instance, () -> parent.start());
            return;
        }

        for (final Player p : Bukkit.getOnlinePlayers()) {
            p.setLevel(this.time);
            if ((this.time <= 5 || this.time == 10) && this.time != 0) {
                Titles.sendTitle(p, 2, 16, 2, ChatColor.GOLD + "Démarrage dans " + ChatColor.RED + this.time + ChatColor.GOLD + " sec", ChatColor.GOLD + "Préparez vous au combat !");
            }
            else if (this.time == 0) {
                Titles.sendTitle(p, 2, 16, 2, ChatColor.GOLD + "C'est parti !", ChatColor.GOLD + "Que le meilleur gagne !");
            }
        }

        if (time <= 5 || time == 10 || time == 15) {
            coherenceMachine.getMessageManager().writeCustomMessage(ChatColor.YELLOW + "Démarrage du jeu dans " + ChatColor.RED + time + " seconde" + ((time > 1) ? "s" : "") + " !", true);
        }

        if (time <= 5 || time == 10) {
            parent.broadcastSound(Sound.NOTE_PIANO);
        }
    }
}
