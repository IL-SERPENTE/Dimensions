package net.samagames.dimensions.tasks;

import net.samagames.api.games.themachine.ICoherenceMachine;
import net.samagames.dimensions.arena.Arena;
import net.samagames.tools.Titles;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/*
 * This file is part of Dimensions.
 *
 * Dimensions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dimensions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Dimensions.  If not, see <http://www.gnu.org/licenses/>.
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
