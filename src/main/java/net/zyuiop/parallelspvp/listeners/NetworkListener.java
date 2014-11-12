package net.zyuiop.parallelspvp.listeners;

import net.samagames.gameapi.events.FinishJoinPlayerEvent;
import net.samagames.gameapi.events.PreJoinPlayerEvent;
import net.zyuiop.parallelspvp.ParallelsPVP;
import net.zyuiop.parallelspvp.arena.Arena;
import net.zyuiop.parallelspvp.arena.ParallelsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;

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
        if (parent.countPlayers() >= parent.getTotalMaxSlots()) {
            event.refuse(ChatColor.RED+"L'arène est pleine.");
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onPostJoin(FinishJoinPlayerEvent event) {
        if (parent.countPlayers() >= parent.getTotalMaxSlots()) {
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
        player.sendMessage(ChatColor.GOLD+"Bienvenue dans "+ChatColor.RED+"Dimensions"+ChatColor.GOLD+" !");
        parent.resetPlayer(player);
        Bukkit.broadcastMessage(ParallelsPVP.pluginTAG+ChatColor.YELLOW+" "+
                player.getName()+
                ChatColor.YELLOW+" a rejoint la partie ! "+reason+
                ChatColor.DARK_GRAY+"[" + ChatColor.RED + nbPlayers + ChatColor.DARK_GRAY + "/" + ChatColor.RED + parent.getMaxPlayers() + ChatColor.DARK_GRAY+"]");

        parent.addPlayer(new ParallelsPlayer(player));

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta bm = (BookMeta)book.getItemMeta();
        bm.setAuthor("SamaGames - zyuiop");
        bm.setTitle("Règles du jeu");
        ArrayList<String> pages = new ArrayList<String>();
        // Typo?
        pages.add(ChatColor.GOLD+"Bienvenue dans "+ChatColor.DARK_AQUA+"Dimensions"+ChatColor.DARK_GREEN+" ! \n\n > Sommaire : "+ChatColor.BLACK+"\n\n P.2: Principe du jeu \n P.3: Dimensions\n P.6: Fonctionnement\n\n\n"+ChatColor.BLACK+"Jeu : zyuiop\nMaps : Amalgar");
        pages.add(ChatColor.DARK_GREEN+"Principe du jeu :"+ChatColor.BLACK+"\n\nLe but du jeu est de trouver un maximum de stuff dans les coffres puis de tuer les autres joueurs afin de rester le dernier en vie.");
        pages.add(ChatColor.DARK_GREEN+"Dimensions :"+ChatColor.BLACK+"\n\nLe jeu s'organise autour de deux dimensions. Changez grâce a l'Ender Eye. \nDécouvrez dans les pages suivantes les secrets de chacune....");
        pages.add(ChatColor.DARK_RED+"Hard Dimension :"+ChatColor.BLACK+"\n\nCette dimension contient des coffres avec du meilleur stuff. Cependant, il n'y a pas de regen de vie et certains effets peuvent vous frapper aléatoirement...");
        pages.add(ChatColor.DARK_GREEN+"Overworld :"+ChatColor.BLACK+"\n\nC'est la dimension par défaut. Ici, aucun effet. Cependant, vous y trouverez moins de coffres et moins de stuff...");
        pages.add(ChatColor.DARK_GREEN+"Fonctionnement :"+ChatColor.BLACK+"\n\n"+ChatColor.GOLD+"Le stuff : \n"+ChatColor.BLACK+"Pendant 3 minutes, le PVP est désactivé. Profitez en bien pour vous stuffer au maximum !\n"+ChatColor.GOLD+"Le PVP :"+ChatColor.BLACK+"\nLorque le PVP s'active, n'ayez aucune pitié pour rester le dernier en vie.");
        bm.setPages(pages);
        // Lets fix the typo
        book.setItemMeta(bm);
        player.getInventory().setItem(0, book);
        player.teleport(parent.getWaitLocation());
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent event) {
        parent.logout(event.getPlayer().getUniqueId());
    }

}
