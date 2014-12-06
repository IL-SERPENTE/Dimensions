package net.zyuiop.parallelspvp;

import net.samagames.gameapi.GameAPI;
import net.samagames.gameapi.json.Status;
import net.samagames.permissionsapi.PermissionsAPI;
import net.samagames.permissionsbukkit.PermissionsBukkit;
import net.samagames.utils.IconMenu;
import net.samagames.utils.IconMenuManager;
import net.zyuiop.parallelspvp.arena.Arena;
import net.zyuiop.parallelspvp.arena.ArenaManager;
import net.zyuiop.parallelspvp.commands.CommandStart;
import net.zyuiop.parallelspvp.listeners.ChestListener;
import net.zyuiop.parallelspvp.listeners.DamageListener;
import net.zyuiop.parallelspvp.listeners.InteractListener;
import net.zyuiop.parallelspvp.listeners.SpectatorListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by zyuiop on 26/09/14.
 */
public class ParallelsPVP extends JavaPlugin {

    protected PermissionsAPI permissionsAPI;
    public static String pluginTAG = ChatColor.DARK_AQUA+"["+ChatColor.AQUA+"Dimensions"+ChatColor.DARK_AQUA+"]";
    protected ArrayList<UUID> joinMod = new ArrayList<UUID>();
    public static ParallelsPVP instance;
    public ArenaManager arenaManager;
    public static IconMenuManager menuManager;
    public static InteractListener interactListener;

    protected boolean testMode = false;

    public void onEnable() {
        this.saveDefaultConfig();
        testMode = this.getConfig().getBoolean("test-mode");

        PermissionsBukkit plugin = (PermissionsBukkit) this.getServer().getPluginManager().getPlugin("SamaPermissionsBukkit");
        permissionsAPI = plugin.getApi();

        menuManager = new IconMenuManager(this);

        World world = Bukkit.getWorlds().get(0);
        File arenaFile = new File(world.getWorldFolder(), "arena.yml");
        if (!arenaFile.exists()) {
            Bukkit.getLogger().severe("#==================[Fatal exception report]==================#");
            Bukkit.getLogger().severe("# The arena.yml description file was NOT FOUND.              #");
            Bukkit.getLogger().severe("# The plugin cannot load without it, please create it.       #");
            Bukkit.getLogger().severe("# The file path is the following :                           #");
            Bukkit.getLogger().severe(arenaFile.getAbsolutePath());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        YamlConfiguration arenaData = YamlConfiguration.loadConfiguration(arenaFile);

        this.arenaManager = new ArenaManager(this, arenaData, arenaFile);

        // Initialisation des listeners
        interactListener = new InteractListener(this);
        Bukkit.getPluginManager().registerEvents(interactListener, this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(this), this);
        new SpectatorListener(this);
        new ChestListener(this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Initialisation de l'arène

        this.getCommand("start").setExecutor(new CommandStart());

        GameAPI.registerGame("parallelspvp", this.getConfig().getInt("com-port", 1234), this.getConfig().getString("BungeeName"));
        instance = this;
        GameAPI.getManager().sendSync();
    }

    public void onDisable() {
        getArena().updateStatus(Status.Stopping);
        GameAPI.getManager().sendSync();
        GameAPI.getManager().disable();
    }

    public static boolean isTesting() {
        return instance.isTestMode();
    }

    public boolean isTestMode() {
        return testMode;
    }

    public PermissionsAPI getApi() {
        return permissionsAPI;
    }

    public Arena getArena() {
        return (Arena) GameAPI.getArena();
    }

    public void joinMod(UUID playerId) {
        if (!joinMod.contains(playerId))
            joinMod.add(playerId);
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

    public boolean asMod(UUID playerId) {
        return joinMod.contains(playerId);
    }

    public void leaveMod(UUID playerId) {
        this.joinMod.remove(playerId);
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
