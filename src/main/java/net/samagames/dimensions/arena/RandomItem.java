package net.samagames.dimensions.arena;

import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Created by zyuiop on 28/09/14.
 * Updated by Rigner on 07/08/16.
 */
public class RandomItem
{
    private ItemStack item;
    private int frequency;
    private int[] quantity = null;

    public RandomItem(ItemStack item, int frequency)
    {
        this.item = item;
        this.frequency = frequency;
    }

    public RandomItem(ItemStack item, int frequency, int[] quantity)
    {
        this.item = item;
        this.frequency = frequency;
        this.quantity = quantity;
    }

    public int getFrequency()
    {
        return frequency;
    }

    public ItemStack getItem()
    {
        return item;
    }

    public int getQuantity()
    {
        if (quantity == null)
            return item.getAmount();
        else
        {
            int size = quantity.length;
            return quantity[new Random().nextInt(size - 1)];
        }
    }
}
