package net.zyuiop.parallelspvp.arena;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by zyuiop on 28/10/14.
 */
public class ParallelsPlayer {

    private UUID playerID;

    public ParallelsPlayer(Player p) {
        this.playerID = p.getUniqueId();
    }

    public ParallelsPlayer(UUID p) {
        this.playerID = p;
    }

    public boolean isSpectator = false;

    @Override
    public boolean equals(Object other) {
        if (other instanceof Player) {
            return (playerID.equals(((Player) other).getUniqueId()));
        } else if (other instanceof UUID) {
            return (playerID.equals((UUID) other));
        } else if (other instanceof ParallelsPlayer || other.getClass().equals(ParallelsPlayer.class)) {
            ParallelsPlayer pl = (ParallelsPlayer) other;
            return (playerID.equals(pl.getPlayerID()));
        } else {
            return false;
        }
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(playerID);
    }

    public UUID getPlayerID() {
        return playerID;
    }

}
