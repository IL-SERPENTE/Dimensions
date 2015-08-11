package net.samagames.dimensions;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.Status;
import net.samagames.api.permissions.IPermissionsManager;
import net.samagames.dimensions.arena.Arena;
import net.samagames.dimensions.commands.CommandStart;
import net.samagames.dimensions.listeners.ChestListener;
import net.samagames.dimensions.listeners.DamageListener;
import net.samagames.dimensions.listeners.InteractListener;
import net.samagames.dimensions.listeners.SpectatorListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by zyuiop on 26/09/14.
 */
public class Dimensions extends JavaPlugin {

    public static Dimensions instance;
    public static InteractListener interactListener;
    public Arena arena;
    protected IPermissionsManager permissionsAPI;
    protected boolean testMode = false;

    public static boolean isTesting() {
        return instance.isTestMode();
    }

    public static ItemStack getCompass() {
        ItemStack i = new ItemStack(Material.COMPASS);
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD+"Trouver les joueurs");
        i.setItemMeta(meta);
        return i;
    }

    public static ItemStack getSwap() {
        ItemStack i = new ItemStack(Material.EYE_OF_ENDER);
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD+"Changer de dimension");
        i.setItemMeta(meta);
        return i;
    }

    public void onEnable() {
        this.saveDefaultConfig();
        testMode = this.getConfig().getBoolean("test-mode");

        permissionsAPI = SamaGamesAPI.get().getPermissionsManager();

        World world = Bukkit.getWorlds().get(0);

        arena = new Arena(this);

        SamaGamesAPI.get().getGameManager().registerGame(arena);

        // Initialisation des listeners
        interactListener = new InteractListener(this);
        Bukkit.getPluginManager().registerEvents(interactListener, this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(this), this);
        new SpectatorListener(this);
        new ChestListener(this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Initialisation de l'arène

        this.getCommand("start").setExecutor(new CommandStart());

        instance = this;
    }

    public void onDisable() {
        getArena().setStatus(Status.REBOOTING);
    }

    public boolean isTestMode() {
        return testMode;
    }

    public IPermissionsManager getApi() {
        return permissionsAPI;
    }

    public Arena getArena() {
        return arena;
    }

    public void kickPlayer(final Player player) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeUTF("Connect");
            out.writeUTF("lobby");
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.sendPluginMessage(this, "BungeeCord", b.toByteArray());
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
                player.kickPlayer("Votre serveur de destination ne répond pas.");
            }
        }, 5*20L);
    }
}
