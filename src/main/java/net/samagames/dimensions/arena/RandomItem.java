package net.samagames.dimensions.arena;

import org.bukkit.inventory.ItemStack;

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
