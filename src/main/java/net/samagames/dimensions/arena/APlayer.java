package net.samagames.dimensions.arena;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.GamePlayer;
import net.samagames.api.shops.IPlayerShop;
import net.samagames.api.shops.IShopsManager;
import net.samagames.dimensions.Dimensions;
import net.samagames.tools.scoreboards.ObjectiveSign;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Geekpower14 on 10/08/2015.
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class APlayer extends GamePlayer {

    private int healAtKill = 0;
    private int strengthAtKill;
    private int healAtStrike;
    private float tpTime = 17;

    private int kills = 0;

    private Player player;

    private ObjectiveSign objectiveInfo;

    public APlayer(Player player) {
        super(player);
        this.player = player;

        objectiveInfo = new ObjectiveSign("infoSide", ChatColor.GREEN + "" + ChatColor.BOLD + "  Dimensions  ");
        objectiveInfo.addReceiver(player);

        updateKills();

        loadShop();
    }

    public void loadShop()
    {
        IPlayerShop shopsManager = SamaGamesAPI.get().getShopsManager().getPlayer(this.uuid);

        strengthAtKill = getData(shopsManager, new int[]{ 134, 135, 136, 137, 138, 139 });

        healAtStrike = getData(shopsManager, new int[]{ 127, 128, 129, 130, 131, 132, 133 });

        healAtKill = getData(shopsManager, new int[]{ 122, 123, 124, 125, 126 });

        tpTime = getData(shopsManager, new int[]{ 117, 118, 119, 120, 121 });

    }

    public int getData(IPlayerShop playerShop, int[] items)
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

    public int getHealAtKill() {
        return healAtKill;
    }

    public void setHealAtKill(int healAtKill) {
        this.healAtKill = healAtKill;
    }

    public int getStrengthAtKill() {
        return strengthAtKill;
    }

    public void setStrengthAtKill(int strenghtAtKill) {
        this.strengthAtKill = strenghtAtKill;
    }

    public int getHealAtStrike() {
        return healAtStrike;
    }

    public void setHealAtStrike(int healAtStrike) {
        this.healAtStrike = healAtStrike;
    }

    protected void updateKills()
    {
        Dimensions.instance.getArena().getObjectiveTab().getScore(getPlayerIfOnline().getName()).setScore(kills);
        Dimensions.instance.getArena().getObjectiveTab().updateScore(true);
    }

    public float getTpTime() {
        return tpTime;
    }

    public void setTpTime(float tpTime) {
        this.tpTime = tpTime;
    }

    public void addKill()
    {
        kills++;
        updateKills();

        objectiveInfo.setLine(5, ChatColor.GRAY + "Kills: " + ChatColor.WHITE + kills);
        objectiveInfo.updateLines();
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public ObjectiveSign getObjectiveInfo() {
        return objectiveInfo;
    }
}
