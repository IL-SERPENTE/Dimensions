package net.zyuiop.parallelspvp.arena;

import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Created by zyuiop on 28/09/14.
 */
public class RandomItem {
    protected ItemStack item;
    protected int[] frequency;
    protected int[] quantity = null;

    public RandomItem(ItemStack item, int frequency) {
        this.item = item;
        this.frequency = new int[]{frequency, frequency*2, frequency*3, frequency*4};
    }

    public RandomItem(ItemStack item, int freq1, int freq2, int freq3, int freq4) {
        this.item = item;
        this.frequency = new int[]{freq1, freq2, freq3, freq4};
    }

    public RandomItem(ItemStack item, int frequency, int[] quantity) {
        this.item = item;
        this.frequency = new int[]{frequency, frequency*2, frequency*3, frequency*5};
        this.quantity = quantity;
    }

    public RandomItem(ItemStack item, int freq1, int freq2, int freq3, int freq4, int[] quantity) {
        this.item = item;
        this.frequency = new int[]{freq1, freq2, freq3, freq4};
        this.quantity = quantity;
    }

    public int getFrequency(int level) {
        if (level > 4)
            return 0;
        return frequency[level-1];
    }

    public ItemStack getItem() {
        return item;
    }

    public int getQuantity() {
        if (quantity == null)
            return item.getAmount();
        else {
            int size = quantity.length;
            return quantity[new Random().nextInt(size)-1];
        }
    }
}
