package net.samagames.parallelspvp.tasks;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.themachine.ICoherenceMachine;
import net.samagames.parallelspvp.arena.APlayer;
import net.samagames.parallelspvp.arena.Arena;
import net.samagames.parallelspvp.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

/**
 * Created by zyuiop on 26/09/14.
 */
public class PVPEnable implements Runnable {

    protected Arena parent;
    protected int time = 181; // 3 Minutes

    protected ICoherenceMachine coherenceMachine;

    public PVPEnable(Arena parentArena) {
        this.parent = parentArena;

        coherenceMachine = SamaGamesAPI.get().getGameManager().getCoherenceMachine();
    }

    @Override
    public void run() {
        time--;
        timeBroadcast();
    }

    public void timeBroadcast() {

        if (time <= 0) {

            for(APlayer aPlayer : parent.getInGamePlayers().values())
            {
                aPlayer.getObjectiveInfo().setLine(1, ChatColor.GREEN + "Let's fight!");
            }

            parent.enablePVP();
            return;
        }else{
            for(APlayer aPlayer : parent.getInGamePlayers().values())
            {
                aPlayer.getObjectiveInfo().setLine(1, ChatColor.YELLOW + "PVP" + ChatColor.GRAY + ": " + ChatColor.WHITE + Utils.secondsToString(time));
            }
        }

        if (time <= 5 || time == 10 || time == 30 || time == 60 || time == 120) {
            coherenceMachine.getMessageManager().writeCustomMessage(ChatColor.GOLD + "Le PVP sera activé dans " + time + " secondes !", true);
        }

        if (time <= 5 || time == 10) {
            parent.broadcastSound(Sound.NOTE_PIANO);
        }
    }
}
