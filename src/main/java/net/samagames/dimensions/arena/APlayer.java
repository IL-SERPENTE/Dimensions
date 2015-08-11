package net.samagames.dimensions.arena;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.GamePlayer;
import net.samagames.api.shops.AbstractShopsManager;
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
        AbstractShopsManager shopsManager = SamaGamesAPI.get().getShopsManager(Dimensions.instance.getArena().getGameCodeName());

        strengthAtKill = Integer.valueOf(getData(shopsManager, "strengthAtKill", "0"));

        healAtStrike = Integer.valueOf(getData(shopsManager, "healAtStrike", "0"));

        healAtKill = Integer.valueOf(getData(shopsManager, "healAtKill", "0"));

        tpTime = Integer.valueOf(getData(shopsManager, "tpTime", "0"));

    }

    public String getData(AbstractShopsManager shopsManager, String key, String defaut)
    {
        String data = shopsManager.getItemLevelForPlayer(player, key);
        return (data != null)? data : defaut;
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

    public int addKill()
    {
        kills++;
        updateKills();

        objectiveInfo.setLine(5, ChatColor.GRAY + "Kills: " + ChatColor.WHITE + kills);
        objectiveInfo.updateLines();
        return kills;
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
