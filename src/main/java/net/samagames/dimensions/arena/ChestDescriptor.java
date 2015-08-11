package net.samagames.dimensions.arena;


import org.bukkit.Location;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Geekpower14 on 10/08/2015.
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class ChestDescriptor
{
    protected Location location;
    protected int lootLevel;

    public ChestDescriptor(final Location location, final int lootLevel) {
        this.location = location;
        this.lootLevel = lootLevel;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    public int getLootLevel() {
        return this.lootLevel;
    }

    public void setLootLevel(final int lootLevel) {
        this.lootLevel = lootLevel;
    }
}
