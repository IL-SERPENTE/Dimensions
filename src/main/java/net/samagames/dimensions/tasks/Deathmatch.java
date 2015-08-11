package net.samagames.dimensions.tasks;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.themachine.ICoherenceMachine;
import net.samagames.dimensions.arena.APlayer;
import net.samagames.dimensions.arena.Arena;
import net.samagames.dimensions.utils.Utils;
import org.bukkit.ChatColor;

/**
 * Created by zyuiop on 26/09/14.
 */
public class Deathmatch implements Runnable {

    protected Arena parent;
    protected int time = 61; // 1 minute

    protected ICoherenceMachine coherenceMachine;

    public Deathmatch(Arena parentArena) {
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
                aPlayer.getObjectiveInfo().setLine(1, ChatColor.GREEN + "Fight final!");
            }

            parent.startDeathMatch();
            return;
        }

        for(APlayer aPlayer : parent.getInGamePlayers().values())
        {
            aPlayer.getObjectiveInfo().setLine(1, ChatColor.YELLOW + "D.Match" + ChatColor.GRAY + ": " + ChatColor.WHITE + Utils.secondsToString(time));
        }

        if (time <= 5 || time == 10 || time == 30 || time == 60) {
            coherenceMachine.getMessageManager().writeCustomMessage(ChatColor.DARK_RED + "Deathmatch dans " + ChatColor.RED + time + " seconde" + ((time > 1) ? "s" : "") + " !", true);
        }
    }
}
