package net.samagames.dimensions.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

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
public class Utils
{
    public static Location srt2Loc(String str)
    {
        String[] locparts = str.split(";");
        if (locparts.length == 5)
            return new Location(Bukkit.getWorlds().get(0), Double.parseDouble(locparts[0]), Double.parseDouble(locparts[1]), Double.parseDouble(locparts[2]), Float.parseFloat(locparts[3]), Float.parseFloat(locparts[4]));
        return new Location(Bukkit.getWorlds().get(0), Double.parseDouble(locparts[0]), Double.parseDouble(locparts[1]), Double.parseDouble(locparts[2]));
    }

    public static String secondsToString(long time)
    {
        int seconds = (int)(time % 60);
        int minutes = (int)(time / 60);
        String secondsStr = (seconds < 10 ? "0" : "") + seconds;
        String minutesStr = (minutes < 10 ? "0" : "") + minutes;
        return minutesStr + ":" + secondsStr;
    }
}
