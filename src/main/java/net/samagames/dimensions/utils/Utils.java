package net.samagames.dimensions.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by Geekpower14 on 09/08/2015.
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class Utils {

    public static Location srt2Loc(String str)
    {
        String[] locparts = str.split(";");
        if (locparts.length == 5) {
            return new Location(Bukkit.getWorlds().get(0), Double.parseDouble(locparts[0]), Double.parseDouble(locparts[1]), Double.parseDouble(locparts[2]), Float.parseFloat(locparts[3]), Float.parseFloat(locparts[4]));
        }
        return new Location(Bukkit.getWorlds().get(0), Double.parseDouble(locparts[0]), Double.parseDouble(locparts[1]), Double.parseDouble(locparts[2]));
    }

    public static String secondsToString(long time){
        int seconds = (int)(time % 60);
        int minutes = (int)(time/60);
        String secondsStr = (seconds<10 ? "0" : "")+ seconds;
        String minutesStr = (minutes<10 ? "0" : "")+ minutes;
        return minutesStr + ":" + secondsStr;
    }

    public static String secondsToStringHours(long time){
        int seconds = (int)(time % 60);
        int minutes = (int)((time/60) % 60);
        int hours = (int)((time/3600) % 24);
        String secondsStr = (seconds<10 ? "0" : "")+ seconds;
        String minutesStr = (minutes<10 ? "0" : "")+ minutes;
        String hoursStr = (hours<10 ? "0" : "")+ hours;
        return hoursStr + ":" + minutesStr + ":" + secondsStr;
    }
}
