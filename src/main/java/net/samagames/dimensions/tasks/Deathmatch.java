package net.samagames.dimensions.tasks;

import net.samagames.api.games.themachine.ICoherenceMachine;
import net.samagames.dimensions.arena.APlayer;
import net.samagames.dimensions.arena.Arena;
import net.samagames.dimensions.utils.Utils;
import org.bukkit.ChatColor;

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
