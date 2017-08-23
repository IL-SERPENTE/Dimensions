package net.samagames.dimensions;

import net.samagames.api.SamaGamesAPI;
import net.samagames.api.games.GamesNames;
import net.samagames.api.games.Status;
import net.samagames.dimensions.arena.Arena;
import net.samagames.dimensions.arena.ArenaStatisticsHelper;
import net.samagames.dimensions.listeners.ChestListener;
import net.samagames.dimensions.listeners.DamageListener;
import net.samagames.dimensions.listeners.InteractListener;
import net.samagames.dimensions.listeners.SpectatorListener;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * This file is part of Dimensions.
 *
 * Dimensions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dimensions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Dimensions.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Dimensions extends JavaPlugin
{
    public static Dimensions instance;
    public static InteractListener interactListener;
    public Arena arena;

    public static ItemStack getCompass()
    {
        ItemStack i = new ItemStack(Material.COMPASS);
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Trouver les joueurs");
        i.setItemMeta(meta);
        return i;
    }

    public static ItemStack getSwap()
    {
        ItemStack i = new ItemStack(Material.EYE_OF_ENDER);
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Changer de dimension");
        i.setItemMeta(meta);
        return i;
    }

    @Override
    public void onEnable()
    {
        this.arena = new Arena(this);

        SamaGamesAPI.get().getGameManager().registerGame(this.arena);
        SamaGamesAPI.get().getGameManager().setGameStatisticsHelper(new ArenaStatisticsHelper());

        // Initialisation des listeners
        Dimensions.interactListener = new InteractListener(this);
        this.getServer().getPluginManager().registerEvents(Dimensions.interactListener, this);
        this.getServer().getPluginManager().registerEvents(new DamageListener(this), this);
        new SpectatorListener(this);
        new ChestListener(this);

        Dimensions.instance = this;

        SamaGamesAPI.get().getShopsManager().setShopToLoad(GamesNames.DIMENSION, true);
        SamaGamesAPI.get().getStatsManager().setStatsToLoad(GamesNames.DIMENSION, true);

        SamaGamesAPI.get().getGameManager().setLegacyPvP(true);
        SamaGamesAPI.get().getGameManager().setKeepPlayerCache(true);
    }

    @Override
    public void onDisable()
    {
        getArena().setStatus(Status.REBOOTING);
    }

    public Arena getArena()
    {
        return this.arena;
    }
}
