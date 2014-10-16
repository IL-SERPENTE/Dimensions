package net.zyuiop.parallelspvp.arena;

import org.bukkit.Location;

/**
 * Created by zyuiop on 26/09/14.
 */
public class ChestDescriptor {
    protected Location location;
    protected int lootLevel;

    public ChestDescriptor(Location location, int lootLevel) {
        this.location = location;
        this.lootLevel = lootLevel;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getLootLevel() {
        return lootLevel;
    }

    public void setLootLevel(int lootLevel) {
        this.lootLevel = lootLevel;
    }
}
