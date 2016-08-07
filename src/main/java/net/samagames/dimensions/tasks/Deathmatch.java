package net.samagames.dimensions.tasks;

import net.samagames.api.games.themachine.ICoherenceMachine;
import net.samagames.dimensions.arena.APlayer;
import net.samagames.dimensions.arena.Arena;
import net.samagames.dimensions.utils.Utils;
import org.bukkit.ChatColor;

/**
 * Created by zyuiop on 26/09/14.
 * Updated by Rigner on 07/08/16.
 */
public class Deathmatch implements Runnable
{
    private Arena parent;
    private int time = 61; // 1 minute

    private ICoherenceMachine coherenceMachine;

    public Deathmatch(Arena parentArena)
    {
        this.parent = parentArena;

        this.coherenceMachine = parentArena.getCoherenceMachine();
    }

    @Override
    public void run()
    {
        this.time--;
        timeBroadcast();
    }

    private void timeBroadcast()
    {
        if (this.time <= 0)
        {
            for (APlayer aPlayer : this.parent.getInGamePlayers().values())
                aPlayer.getObjectiveInfo().setLine(1, ChatColor.GREEN + "Fight final!");

            this.parent.startDeathMatch();
            return ;
        }

        for (APlayer aPlayer : this.parent.getInGamePlayers().values())
            aPlayer.getObjectiveInfo().setLine(1, ChatColor.YELLOW + "D.Match" + ChatColor.GRAY + ": " + ChatColor.WHITE + Utils.secondsToString(this.time));

        if (this.time <= 5 || this.time == 10 || this.time == 30 || this.time == 60)
            this.coherenceMachine.getMessageManager().writeCustomMessage(ChatColor.DARK_RED + "Deathmatch dans " + ChatColor.RED + this.time + " seconde" + ((this.time > 1) ? "s" : "") + " !", true);
    }
}
