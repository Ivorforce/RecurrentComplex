/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils.accessor;

import ivorius.mcopts.accessor.AccessorBiomeDictionary;
import net.minecraftforge.common.BiomeDictionary;

import java.util.List;
import java.util.Map;

/**
 * Created by lukas on 12.12.16.
 */
public class RCAccessorBiomeDictionary
{
    public static BiomeDictionary.Type getTypeWeak(String var)
    {
        return getMap().get(var.toUpperCase());
    }

    public static Map<String, BiomeDictionary.Type> getMap()
    {
        return AccessorBiomeDictionary.getMap();
    }

    public static void addSubtypes(BiomeDictionary.Type type, BiomeDictionary.Type... subtypes)
    {
        AccessorBiomeDictionary.addSubtypes(type, subtypes);
    }

    public static void addSubtypes(BiomeDictionary.Type type, List<BiomeDictionary.Type> subtypes)
    {
        AccessorBiomeDictionary.addSubtypes(type, subtypes);
    }

    public static List<BiomeDictionary.Type> getSubtypes(BiomeDictionary.Type type)
    {
        return AccessorBiomeDictionary.getSubtypes(type);
    }

    public static void setSubtypes(BiomeDictionary.Type type, List<BiomeDictionary.Type> types)
    {
        AccessorBiomeDictionary.setSubtypes(type, types);
    }
}
