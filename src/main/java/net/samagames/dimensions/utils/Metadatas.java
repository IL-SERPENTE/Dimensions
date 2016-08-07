package net.samagames.dimensions.utils;

import net.samagames.dimensions.Dimensions;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

import java.util.List;

/**
 * Created by zyuiop on 26/09/14.
 * Updated by Rigner on 07/08/16.
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
