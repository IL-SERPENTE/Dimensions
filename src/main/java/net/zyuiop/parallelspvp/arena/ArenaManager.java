package net.zyuiop.parallelspvp.arena;

import net.samagames.gameapi.GameAPI;
import net.zyuiop.parallelspvp.ParallelsPVP;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by zyuiop on 05/10/14.
 */
public class ArenaManager {
    public ArenaManager(ParallelsPVP plugin, YamlConfiguration arenaData, File arenaFile) {
        int maxPlayers = arenaData.getInt("max-players");
        int vipSlots = arenaData.getInt("vip-players");
        UUID arenaId = UUID.fromString(arenaData.getString("uuid", UUID.randomUUID().toString()));
        arenaData.set("uuid", arenaId.toString());
        try {
            arenaData.save(arenaFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Arena ar = new Arena(plugin, arenaData, maxPlayers, vipSlots, arenaId);
        GameAPI.registerArena(ar);
    }
}
