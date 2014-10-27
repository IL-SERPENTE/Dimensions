package net.zyuiop.parallelspvp.listeners;

import net.zyuiop.parallelspvp.ParallelsPVP;
import net.zyuiop.parallelspvp.arena.ChestDescriptor;
import net.zyuiop.parallelspvp.arena.RandomItem;
import net.zyuiop.parallelspvp.utils.Metadatas;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by zyuiop on 26/09/14.
 */
public class ChestListener implements Listener {

    protected ParallelsPVP plugin;
    protected ArrayList<RandomItem> items = new ArrayList<RandomItem>();

    public ChestListener(ParallelsPVP plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Ici on fait les registers de chaque item //

        // ARMURES //
        registerItem(new RandomItem(new ItemStack(Material.LEATHER_LEGGINGS, 1), 200, 400, 200, 0));
        registerItem(new RandomItem(new ItemStack(Material.LEATHER_BOOTS, 1), 200, 400, 200, 0));
        registerItem(new RandomItem(new ItemStack(Material.LEATHER_CHESTPLATE, 1), 200, 400, 200, 0));
        registerItem(new RandomItem(new ItemStack(Material.LEATHER_HELMET, 1), 200, 400, 200, 0));

        registerItem(new RandomItem(new ItemStack(Material.CHAINMAIL_HELMET, 1), 150, 300, 450, 50));
        registerItem(new RandomItem(new ItemStack(Material.CHAINMAIL_BOOTS, 1), 150, 300, 450, 50));
        registerItem(new RandomItem(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1), 100, 200, 300, 50));
        registerItem(new RandomItem(new ItemStack(Material.CHAINMAIL_LEGGINGS, 1), 100, 200, 300, 50));

        registerItem(new RandomItem(new ItemStack(Material.IRON_LEGGINGS, 1), 0, 0, 50, 500));
        registerItem(new RandomItem(new ItemStack(Material.IRON_BOOTS, 1), 0, 0, 50, 500));
        registerItem(new RandomItem(new ItemStack(Material.IRON_CHESTPLATE, 1), 0, 0, 40, 400));
        registerItem(new RandomItem(new ItemStack(Material.IRON_HELMET, 1), 0, 0, 50, 500));

        // OUTILS ET ARMES //
        registerItem(new RandomItem(new ItemStack(Material.STONE_PICKAXE, 1), 300, 200, 0, 0));
        registerItem(new RandomItem(new ItemStack(Material.STONE_SWORD, 1), 200, 150, 100, 0));
        registerItem(new RandomItem(new ItemStack(Material.STONE_AXE, 1), 300, 100, 0, 0));
        registerItem(new RandomItem(new ItemStack(Material.IRON_SWORD, 1), 0, 50, 100, 200));
        registerItem(new RandomItem(new ItemStack(Material.DIAMOND_SWORD, 1), 0, 1, 15, 70));
        registerItem(new RandomItem(new ItemStack(Material.WOOD_SWORD, 1), 350, 100, 0, 0));

        // RESSOURCES //
        registerItem(new RandomItem(new ItemStack(Material.IRON_INGOT), 200, 400, 600, 800, new int[]{2, 3, 4, 5, 6, 7}));
        registerItem(new RandomItem(new ItemStack(Material.BAKED_POTATO), 300, 200, 150, 100, new int[]{4, 5, 6, 7, 8, 9, 10}));
        registerItem(new RandomItem(new ItemStack(Material.COOKED_BEEF), 300, 200, 150, 100, new int[]{2, 3, 4, 5}));
        registerItem(new RandomItem(new ItemStack(Material.EXP_BOTTLE), 200, 300, 400, 400, new int[]{4, 5, 6, 7}));
        registerItem(new RandomItem(new ItemStack(Material.LOG), 400, 300, 200, 150, new int[]{2, 3, 4}));
        registerItem(new RandomItem(new ItemStack(Material.GLOWSTONE_DUST, 1), 20, 40, 60, 80));
        registerItem(new RandomItem(new ItemStack(Material.STRING), 100, 200, 300, 450, new int[]{2, 3, 4}));
        registerItem(new RandomItem(new ItemStack(Material.FEATHER, 1), 50, 100, 150, 150));
        registerItem(new RandomItem(new ItemStack(Material.FLINT), 50, 100, 200, 250, new int[]{2, 3}));
        registerItem(new RandomItem(new ItemStack(Material.COBBLESTONE), 100, 150, 200, 150, new int[]{3, 4, 5, 6}));

        // POTIONS //
        registerItem(new RandomItem(new Potion(PotionType.INSTANT_HEAL).toItemStack(1), 50, 100, 150, 200));
        registerItem(new RandomItem(new Potion(PotionType.INSTANT_HEAL).splash().toItemStack(1), 50, 100, 150, 200));
        registerItem(new RandomItem(new Potion(PotionType.INSTANT_HEAL, 2).toItemStack(1), 150, 300, 450, 450));
        registerItem(new RandomItem(new Potion(PotionType.STRENGTH).toItemStack(1), 50, 100, 150, 200));
        registerItem(new RandomItem(new Potion(PotionType.REGEN).toItemStack(1), 50, 100, 150, 200));
        registerItem(new RandomItem(new Potion(PotionType.POISON).splash().toItemStack(1), 50, 100, 150, 200));
        registerItem(new RandomItem(new Potion(PotionType.INSTANT_DAMAGE).splash().toItemStack(1), 50, 100, 150, 200));
        registerItem(new RandomItem(new Potion(PotionType.SPEED).toItemStack(1), 50, 100, 150, 200));

        // Enchants
        ItemStack sharpness = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) sharpness.getItemMeta();
        meta.addStoredEnchant(Enchantment.DAMAGE_ALL, 1, false);
        sharpness.setItemMeta(meta);

        ItemStack protection = new ItemStack(Material.ENCHANTED_BOOK);
        meta = (EnchantmentStorageMeta) protection.getItemMeta();
        meta.addStoredEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1, false);
        protection.setItemMeta(meta);

        registerItem(new RandomItem(sharpness, 50, 100, 150, 200));
        registerItem(new RandomItem(protection, 50, 100, 150, 200));

        // MISC //
        registerItem(new RandomItem(new ItemStack(Material.ARROW), 150, 300, 450, 450, new int[]{3, 4, 5, 6, 7, 8, 9, 10}));
        registerItem(new RandomItem(new ItemStack(Material.GOLDEN_APPLE), 0, 10, 20, 50));
        registerItem(new RandomItem(new ItemStack(Material.TNT), 100, 100, 200, 300, new int[]{1, 3}));
        registerItem(new RandomItem(new ItemStack(Material.APPLE), 200, 100, 50, 0));

        ItemStack axe = new ItemStack(Material.IRON_AXE);
        ItemMeta imeta = axe.getItemMeta();
        imeta.setDisplayName(ChatColor.GOLD+"Hache du Troll des Cavernes");
        axe.setItemMeta(imeta);
        axe.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
        axe.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
        axe.addUnsafeEnchantment(Enchantment.DURABILITY, 2);
        registerItem(new RandomItem(axe, 0, 0, 0, 50));
        registerItem(new RandomItem(new ItemStack(Material.GOLDEN_APPLE, 1, (short)1), 0, 0, 0, 2));
    }

    public void registerItem(RandomItem item) {
        this.items.add(item);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChestOpen(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType().equals(Material.CHEST)) {
            Chest chest = (Chest) event.getClickedBlock().getState();
            ChestDescriptor info = plugin.getArena().getChestFromLocation(event.getClickedBlock().getLocation());
            if (info == null)
                return;

            Boolean wasOpened = (Boolean) Metadatas.getMetadata(chest, "opened");
            if (wasOpened == null || !wasOpened) {
                Metadatas.setMetadata(chest, "opened", true);

                // Ici, on set le contenu du coffre
                Inventory inv = chest.getInventory();
                inv.clear();

                Collections.shuffle(items); // On shuffle a chaque fois. Si trop long faudra passer en async
                int addedItems = 0;
                Random rnd = new Random();
                int slot = 0;
                for (RandomItem item : items) {
                    if (addedItems > 15)
                        break;

                    int freq = item.getFrequency(info.getLootLevel());
                    if (freq == 0)
                        continue;
                    if (rnd.nextInt(1000) <= freq) {
                        ItemStack stack = item.getItem();
                        stack.setAmount(item.getQuantity());
                        inv.setItem(slot, item.getItem());
                        addedItems++;
                    }
                    slot++;
                }

            }
        }
    }
}
