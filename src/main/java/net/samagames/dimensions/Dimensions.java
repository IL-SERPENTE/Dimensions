package net.samagames.dimensions;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.GamesNames;
import net.samagames.api.games.Status;
import net.samagames.api.permissions.IPermissionsManager;
import net.samagames.dimensions.arena.Arena;
import net.samagames.dimensions.listeners.ChestListener;
import net.samagames.dimensions.listeners.DamageListener;
import net.samagames.dimensions.listeners.InteractListener;
import net.samagames.dimensions.listeners.SpectatorListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

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

    @Override
    public void onEnable() {
        testMode = false;

        permissionsAPI = SamaGamesAPI.get().getPermissionsManager();

        arena = new Arena(this);

        SamaGamesAPI.get().getGameManager().registerGame(arena);

        // Initialisation des listeners
        interactListener = new InteractListener(this);
        Bukkit.getPluginManager().registerEvents(interactListener, this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(this), this);
        new SpectatorListener(this);
        new ChestListener(this);

        instance = this;

        SamaGamesAPI.get().getShopsManager().setShopToLoad(GamesNames.DIMENSION, true);
        SamaGamesAPI.get().getStatsManager().setStatsToLoad(GamesNames.DIMENSION, true);

        SamaGamesAPI.get().getGameManager().setLegacyPvP(true);
        SamaGamesAPI.get().getGameManager().setKeepPlayerCache(true);
    }

    @Override
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
        SamaGamesAPI.get().getGameManager().kickPlayer(player, null);
    }
}
