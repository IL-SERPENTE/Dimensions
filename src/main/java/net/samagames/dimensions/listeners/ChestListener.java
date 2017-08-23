package net.samagames.dimensions.listeners;

import net.samagames.dimensions.Dimensions;
import net.samagames.dimensions.arena.RandomItem;
import net.samagames.dimensions.utils.Metadatas;
import net.samagames.tools.MojangShitUtils;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
public class ChestListener implements Listener
{
    private List<RandomItem> items = new ArrayList<>();
    private Dimensions plugin;

    @SuppressWarnings("deprecation")
    public ChestListener(Dimensions plugin)
    {        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Ici on fait les registers de chaque item //

        // ARMURES //
        this.registerItem(new RandomItem(new ItemStack(Material.LEATHER_LEGGINGS, 1), 700));
        this.registerItem(new RandomItem(new ItemStack(Material.LEATHER_BOOTS, 1), 1000));
        this.registerItem(new RandomItem(new ItemStack(Material.LEATHER_CHESTPLATE, 1), 700));
        this.registerItem(new RandomItem(new ItemStack(Material.LEATHER_HELMET, 1), 1000));

        this.registerItem(new RandomItem(new ItemStack(Material.CHAINMAIL_HELMET, 1), 700));
        this.registerItem(new RandomItem(new ItemStack(Material.CHAINMAIL_BOOTS, 1), 700));
        this.registerItem(new RandomItem(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1), 1000));
        this.registerItem(new RandomItem(new ItemStack(Material.CHAINMAIL_LEGGINGS, 1), 1000));

        this.registerItem(new RandomItem(new ItemStack(Material.IRON_LEGGINGS, 1), 700));
        this.registerItem(new RandomItem(new ItemStack(Material.IRON_BOOTS, 1), 700));
        this.registerItem(new RandomItem(new ItemStack(Material.IRON_CHESTPLATE, 1), 700));
        this.registerItem(new RandomItem(new ItemStack(Material.IRON_HELMET, 1), 700));

        this.registerItem(new RandomItem(new ItemStack(Material.DIAMOND_LEGGINGS, 1), 25));
        this.registerItem(new RandomItem(new ItemStack(Material.DIAMOND_BOOTS, 1), 25));
        this.registerItem(new RandomItem(new ItemStack(Material.DIAMOND_CHESTPLATE, 1), 25));
        this.registerItem(new RandomItem(new ItemStack(Material.DIAMOND_HELMET, 1), 25));

        // OUTILS ET ARMES //
        this.registerItem(new RandomItem(new ItemStack(Material.STONE_PICKAXE, 1), 1000));
        this.registerItem(new RandomItem(new ItemStack(Material.STONE_SWORD, 1), 2500));
        this.registerItem(new RandomItem(new ItemStack(Material.IRON_SWORD, 1), 500));
        this.registerItem(new RandomItem(new ItemStack(Material.DIAMOND_SWORD, 1), 50));

        // RESSOURCES //
        this.registerItem(new RandomItem(new ItemStack(Material.IRON_INGOT), 4000, new int[]{2, 3, 4, 5, 6}));
        this.registerItem(new RandomItem(new ItemStack(Material.DIAMOND), 50, new int[]{1,2,3}));
        this.registerItem(new RandomItem(new ItemStack(Material.COOKED_CHICKEN), 2000, new int[]{4, 5, 6, 7, 8, 9, 10}));
        this.registerItem(new RandomItem(new ItemStack(Material.COOKED_BEEF), 5000, new int[]{2, 3, 4, 5}));
        this.registerItem(new RandomItem(new ItemStack(Material.EXP_BOTTLE), 1000, new int[]{5, 6, 7, 8, 9, 10, 11, 12}));
        final Dye dye = new Dye();
        dye.setColor(DyeColor.BLUE);
        this.registerItem(new RandomItem(dye.toItemStack(), 3000, new int[]{4, 5, 6, 7, 8}));
        this.registerItem(new RandomItem(new ItemStack(Material.STICK), 2000, new int[]{2, 3, 4, 5}));
        this.registerItem(new RandomItem(new ItemStack(Material.WORKBENCH, 1), 1000));
        this.registerItem(new RandomItem(new ItemStack(Material.BOW), 1000));

        // POTIONS //
        this.registerItem(new RandomItem(new Potion(PotionType.INSTANT_HEAL).splash().toItemStack(1), 800));
        this.registerItem(new RandomItem(new Potion(PotionType.REGEN).toItemStack(1), 100));
        this.registerItem(new RandomItem(MojangShitUtils.getPotion("poison", true, true), 500));
        this.registerItem(new RandomItem(new Potion(PotionType.INSTANT_DAMAGE).splash().toItemStack(1), 500));
        this.registerItem(new RandomItem(new Potion(PotionType.SPEED).toItemStack(1), 500));

        // Enchants
        ItemStack sharpness = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) sharpness.getItemMeta();
        meta.addStoredEnchant(Enchantment.DAMAGE_ALL, 1, false);
        sharpness.setItemMeta(meta);

        ItemStack protection = new ItemStack(Material.ENCHANTED_BOOK);
        meta = (EnchantmentStorageMeta) protection.getItemMeta();
        meta.addStoredEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, false);
        protection.setItemMeta(meta);

        this.registerItem(new RandomItem(sharpness, 700));
        this.registerItem(new RandomItem(protection, 700));

        ItemStack bow = new ItemStack(Material.BOW);
        bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
        this.registerItem(new RandomItem(bow, 300));
        // MISC //
        this.registerItem(new RandomItem(new ItemStack(Material.ARROW), 3000, new int[]{3, 4, 5, 6, 7, 8, 9, 10}));
        this.registerItem(new RandomItem(new ItemStack(Material.GOLDEN_APPLE), 500));

        ItemStack axe = new ItemStack(Material.IRON_AXE);
        ItemMeta imeta = axe.getItemMeta();
        imeta.setDisplayName(ChatColor.GOLD + "Hache de papy Sama");
        axe.setItemMeta(imeta);
        axe.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
        axe.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
        axe.addUnsafeEnchantment(Enchantment.DURABILITY, 2);
        this.registerItem(new RandomItem(axe, 5));
        this.registerItem(new RandomItem(new ItemStack(Material.GOLDEN_APPLE, 1, (short)1), 2));

        // NEW RANDOM ITEMS //
        this.registerItem(new RandomItem(new ItemStack(Material.SHIELD, 1), 200));
        this.registerItem(new RandomItem(new ItemStack(Material.ELYTRA, 1), 50));
        this.registerItem(new RandomItem(new ItemStack(Material.WATER_BUCKET, 1), 100));
        this.registerItem(new RandomItem(new ItemStack(Material.LAVA_BUCKET, 1), 50));
        this.registerItem(new RandomItem(new ItemStack(Material.ENDER_PEARL), 100, new int[]{1, 2, 3}));
    }

    private static void launchfw(Dimensions plugin, Location loc, final FireworkEffect effect)
    {
        loc = loc.add(0.5,0.5,0.5);
        final Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.addEffect(effect);
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);
        plugin.getServer().getScheduler().runTaskLater(Dimensions.instance, fw::detonate, 2);
    }

    private void registerItem(RandomItem item)
    {
        this.items.add(item);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChestOpen(PlayerInteractEvent event)
    {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType().equals(Material.CHEST))
        {
            Chest chest = (Chest) event.getClickedBlock().getState();

            Boolean wasOpened = (Boolean) Metadatas.getMetadata(chest, "opened");
            if (wasOpened == null || !wasOpened)
            {
                Metadatas.setMetadata(chest, "opened", true);

                // Ici, on set le contenu du coffre
                Inventory inv = chest.getInventory();
                inv.clear();

                Collections.shuffle(this.items); // On shuffle a chaque fois. Si trop long faudra passer en async
                int addedItems = 0;
                Random rnd = new Random();
                int slot = 0;
                for (RandomItem item : this.items)
                {
                    if (addedItems > 20)
                        break ;

                    int freq = item.getFrequency();
                    if (rnd.nextInt(10000) <= freq)
                    {
                        ItemStack stack = item.getItem();
                        stack.setAmount(item.getQuantity());
                        while (inv.getItem(slot) != null)
                            slot++;
                        inv.setItem(slot, item.getItem());
                        addedItems++;
                    }
                    slot++;

                    if (slot > 26)
                        slot = 0;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event)
    {
        final InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Chest)
        {
            final Chest chest = (Chest)holder;
            launchfw(this.plugin, chest.getLocation(), FireworkEffect.builder().withColor(new Color[] { Color.WHITE, Color.GRAY, Color.BLACK }).with(FireworkEffect.Type.STAR).build());
            chest.getBlock().setType(Material.AIR);
        }
    }
}
