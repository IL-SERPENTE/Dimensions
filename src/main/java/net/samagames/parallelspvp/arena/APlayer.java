package net.samagames.parallelspvp.arena;

import net.samagames.api.games.GamePlayer;
import net.samagames.parallelspvp.ParallelsPVP;
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
    private int strenghtAtKill;
    private int healAtStrike;
    private float tpTime = 17;

    private int kills = 0;

    private ObjectiveSign objectiveInfo;

    public APlayer(Player player) {
        super(player);

        objectiveInfo = new ObjectiveSign("infoSide", ChatColor.GREEN + "" + ChatColor.BOLD + "  Dimensions  ");
        objectiveInfo.addReceiver(player);

        updateKills();
    }

    public int getHealAtKill() {
        return healAtKill;
    }

    public void setHealAtKill(int healAtKill) {
        this.healAtKill = healAtKill;
    }

    public int getStrenghtAtKill() {
        return strenghtAtKill;
    }

    public void setStrenghtAtKill(int strenghtAtKill) {
        this.strenghtAtKill = strenghtAtKill;
    }

    public int getHealAtStrike() {
        return healAtStrike;
    }

    public void setHealAtStrike(int healAtStrike) {
        this.healAtStrike = healAtStrike;
    }

    protected void updateKills()
    {
        ParallelsPVP.instance.getArena().getObjectiveTab().getScore(getPlayerIfOnline().getName()).setScore(kills);
        ParallelsPVP.instance.getArena().getObjectiveTab().updateScore(true);
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
