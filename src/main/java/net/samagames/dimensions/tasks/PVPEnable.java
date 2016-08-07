package net.samagames.dimensions.tasks;

import net.samagames.api.games.themachine.ICoherenceMachine;
import net.samagames.dimensions.arena.APlayer;
import net.samagames.dimensions.arena.Arena;
import net.samagames.dimensions.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

/**
 * Created by zyuiop on 26/09/14.
 * Updated by Rigner on 07/08/16.
 */
public class PVPEnable implements Runnable
{
    private Arena parent;
    private int time = 121; // 2 Minutes

    private ICoherenceMachine coherenceMachine;

    public PVPEnable(Arena parentArena)
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
                aPlayer.getObjectiveInfo().setLine(1, ChatColor.GREEN + "Let's fight!");

            this.parent.enablePVP();
            return ;
        }
        else
            for (APlayer aPlayer : this.parent.getInGamePlayers().values())
                aPlayer.getObjectiveInfo().setLine(1, ChatColor.YELLOW + "PVP" + ChatColor.GRAY + ": " + ChatColor.WHITE + Utils.secondsToString(time));

        if (this.time <= 5 || this.time == 10 || this.time == 30 || this.time == 60 || this.time == 120)
            this.coherenceMachine.getMessageManager().writeCustomMessage(ChatColor.GOLD + "Le PVP sera activé dans " + this.time + " secondes !", true);

        if (this.time <= 5 || this.time == 10)
            parent.broadcastSound(Sound.BLOCK_NOTE_HARP);
    }
}
