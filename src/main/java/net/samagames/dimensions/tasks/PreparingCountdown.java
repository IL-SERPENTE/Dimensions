package net.samagames.dimensions.tasks;

import net.samagames.api.games.themachine.ICoherenceMachine;
import net.samagames.dimensions.arena.Arena;
import net.samagames.tools.Titles;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * Created by zyuiop on 26/09/14.
 * Updated by Rigner on 07/08/16.
 */
public class PreparingCountdown implements Runnable, Listener
{
    private Arena parent;
    private int time = 16; // 15 secondes

    private ICoherenceMachine coherenceMachine;

    public PreparingCountdown(Arena parentArena)
    {
        this.parent = parentArena;

        this.coherenceMachine = parentArena.getCoherenceMachine();
    }

    @Override
    public void run()
    {
        this.time --;
        timeBroadcast();
    }

    private void timeBroadcast()
    {
        if (time == 0)
        {
            this.parent.getPlugin().getServer().getScheduler().runTask(this.parent.getPlugin(), this.parent::start);
            return;
        }

        for (final Player p : this.parent.getPlugin().getServer().getOnlinePlayers())
        {
            p.setLevel(this.time);
            if ((this.time <= 5 || this.time == 10) && this.time != 0)
                Titles.sendTitle(p, 2, 16, 2, ChatColor.GOLD + "Début dans " + ChatColor.RED + this.time + ChatColor.GOLD + " sec", ChatColor.GOLD + "Préparez vous au combat !");
            else if (this.time == 0)
                Titles.sendTitle(p, 2, 16, 2, ChatColor.GOLD + "C'est parti !", ChatColor.GOLD + "Que le meilleur gagne !");
        }

        if (this.time <= 5 || time == 10 || time == 15)
            this.coherenceMachine.getMessageManager().writeCustomMessage(ChatColor.YELLOW + "Début du jeu dans " + ChatColor.RED + this.time + " seconde" + ((this.time > 1) ? "s" : "") + " !", true);

        if (this.time <= 5 || this.time == 10)
            this.parent.broadcastSound(Sound.BLOCK_NOTE_HARP);
    }
}
