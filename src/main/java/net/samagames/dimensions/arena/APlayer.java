package net.samagames.dimensions.arena;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.GamePlayer;
import net.samagames.api.shops.IPlayerShop;
import net.samagames.dimensions.Dimensions;
import net.samagames.tools.scoreboards.ObjectiveSign;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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
public class APlayer extends GamePlayer
{

    private int healAtKill = 0;
    private int strengthAtKill;
    private int healAtStrike;
    private float tpTime = 17;

    private int kills = 0;

    private ObjectiveSign objectiveInfo;

    public APlayer(Player player)
    {
        super(player);

        this.objectiveInfo = new ObjectiveSign("infoSide", ChatColor.GREEN + "" + ChatColor.BOLD + "  Dimensions  ");
        this.objectiveInfo.addReceiver(player);

        updateKills();

        loadShop();
    }

    private void loadShop()
    {
        IPlayerShop shopsManager = SamaGamesAPI.get().getShopsManager().getPlayer(this.uuid);

        this.strengthAtKill = getData(shopsManager, new int[]{ 134, 135, 136, 137, 138, 139 });

        this.healAtStrike = getData(shopsManager, new int[]{ 127, 128, 129, 130, 131, 132, 133 });

        this.healAtKill = getData(shopsManager, new int[]{ 122, 123, 124, 125, 126 }) * 2;

        this.tpTime = getData(shopsManager, new int[]{ 117, 118, 119, 120, 121 });

        this.strengthAtKill = new int[]{ 3, 3, 5, 7, 9, 10, 11 }[this.strengthAtKill];

        this.healAtStrike = new int[]{ 2, 2, 4, 6, 7, 8, 9, 10 }[this.healAtStrike];

        this.tpTime = new float[]{ 14, 14, 12, 10, 8, 7 }[(int)this.tpTime];
    }

    private int getData(IPlayerShop playerShop, int[] items)
    {
        try
        {
            int selected = playerShop.getSelectedItemFromList(items);
            for (int i = 0; i < items.length; i++)
                if (items[i] == selected)
                    return i + 1;
            return 0;
        }
        catch (Exception ex)
        {
            return 0;
        }
    }

    public int getHealAtKill()
    {
        return this.healAtKill;
    }

    public int getStrengthAtKill()
    {
        return this.strengthAtKill;
    }

    public int getHealAtStrike()
    {
        return this.healAtStrike;
    }

    private void updateKills()
    {
        Dimensions.instance.getArena().getObjectiveTab().getScore(getPlayerIfOnline().getName()).setScore(kills);
        Dimensions.instance.getArena().getObjectiveTab().updateScore(true);
    }

    float getTpTime()
    {
        return this.tpTime;
    }

    public void addKill()
    {
        this.kills++;
        updateKills();

        this.objectiveInfo.setLine(5, ChatColor.GRAY + "Kills: " + ChatColor.WHITE + kills);
        this.objectiveInfo.updateLines();
    }

    int getKills()
    {
        return this.kills;
    }

    public ObjectiveSign getObjectiveInfo()
    {
        return this.objectiveInfo;
    }
}
