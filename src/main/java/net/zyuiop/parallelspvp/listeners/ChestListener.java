package net.zyuiop.parallelspvp.listeners;

import net.zyuiop.parallelspvp.ParallelsPVP;
import net.zyuiop.parallelspvp.arena.ChestDescriptor;
import net.zyuiop.parallelspvp.arena.RandomItem;
import net.zyuiop.parallelspvp.utils.Metadatas;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
        registerItem(new RandomItem(new ItemStack(Material.STONE_SWORD, 1), 200));
        registerItem(new RandomItem(new ItemStack(Material.IRON_INGOT), 200, new int[]{2,3,4,5,6,7,8,9,10}));
        registerItem(new RandomItem(new ItemStack(Material.WOOD, 1), 330, new int[]{2,3,4,5}));
        registerItem(new RandomItem(new ItemStack(Material.COOKED_BEEF, 1), 330, new int[]{2,3,4,5}));
        registerItem(new RandomItem(new ItemStack(Material.LEATHER_LEGGINGS, 1), 100));
        registerItem(new RandomItem(new ItemStack(Material.LEATHER_BOOTS, 1), 200));
        registerItem(new RandomItem(new ItemStack(Material.LEATHER_CHESTPLATE, 1), 100));
        registerItem(new RandomItem(new ItemStack(Material.LEATHER_HELMET, 1), 100));
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
                for (RandomItem item : items) {
                    if (addedItems > 15)
                        break;

                    int freq = item.getFrequency(info.getLootLevel());
                    if (freq == 0)
                        continue;
                    if (rnd.nextInt(1000) <= freq) {
                        ItemStack stack = item.getItem();
                        stack.setAmount(item.getQuantity());
                        inv.addItem(item.getItem());
                        addedItems++;
                    }
                }

            }
        }
    }
}
