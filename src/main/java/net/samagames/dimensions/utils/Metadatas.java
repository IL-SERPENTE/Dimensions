package net.samagames.dimensions.utils;

import net.samagames.dimensions.Dimensions;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

import java.util.List;

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
public class Metadatas
{
    public static Object getMetadata(Metadatable object, String key)
    {
        List<MetadataValue> values = object.getMetadata(key);
        for (MetadataValue value : values)
        {
            // Plugins are singleton objects, so using == is safe here
            if (value.getOwningPlugin() == Dimensions.instance)
                return value.value();
        }
        return null;
    }

    public static void setMetadata(Metadatable object, String key, Object value)
    {
        object.setMetadata(key, new FixedMetadataValue(Dimensions.instance,value));
    }
}
