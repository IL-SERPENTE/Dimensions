package net.zyuiop.parallelspvp.listeners;

import net.samagames.gameapi.events.FinishJoinPlayerEvent;
import net.samagames.gameapi.events.PreJoinPlayerEvent;
import net.zyuiop.parallelspvp.ParallelsPVP;
import net.zyuiop.parallelspvp.arena.Arena;
import net.zyuiop.parallelspvp.arena.ParallelsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by zyuiop on 27/10/14.
 */
public class NetworkListener implements Listener {

    protected Arena parent;

    public NetworkListener(Arena parent) {
        this.parent = parent;
    }

    @EventHandler (ignoreCancelled = true)
    public void onPreJoin(PreJoinPlayerEvent event) {
        if (parent.countPlayers() > parent.getTotalMaxSlots()) {
            event.refuse(ChatColor.RED+"L'arène est pleine.");
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onPostJoin(FinishJoinPlayerEvent event) {
        if (parent.countPlayers() > parent.getTotalMaxSlots()) {
            event.refuse(ChatColor.RED+"L'arène est pleine.");
            return;
        }

        Player player = Bukkit.getPlayer(event.getPlayer());
        if (player == null)
            event.refuse(ChatColor.RED+"Une erreur de connexion s'est produite.");

        int nbPlayers = parent.countPlayers() + 1;

        String reason = "";
        if (nbPlayers > parent.getMaxPlayers())
            reason = ChatColor.GREEN+"[Slots Donateurs] ";

        // Setup du joueur
        player.sendMessage(ChatColor.GOLD+"Bienvenue dans "+ChatColor.RED+"Parallels PVP"+ChatColor.GOLD+" !");
        parent.resetPlayer(player);
        Bukkit.broadcastMessage(ParallelsPVP.pluginTAG+ChatColor.YELLOW+" "+
                player.getName()+
                ChatColor.YELLOW+" a rejoint la partie ! "+reason+
                ChatColor.DARK_GRAY+"[" + ChatColor.RED + nbPlayers + ChatColor.DARK_GRAY + "/" + ChatColor.RED + parent.getMaxPlayers() + ChatColor.DARK_GRAY+"]");

        parent.addPlayer(new ParallelsPlayer(player));


        player.teleport(parent.getWaitLocation());
    }

}
