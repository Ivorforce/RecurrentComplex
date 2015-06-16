/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import ivorius.reccomplex.RecurrentComplex;

import java.util.*;

/**
 * Created by lukas on 25.05.14.
 */
public class WeightedItemCollectionRegistry
{
    private static Map<String, WeightedItemCollection> weightedItemCollectionMap = new HashMap<>();

    public static void register(WeightedItemCollection weightedItemCollection, String key)
    {
//        RecurrentComplex.logger.info(weightedItemCollectionMap.containsKey(key) ? "Replaced inventory generator '" + key + "'" : "Registered inventory generator '" + key + "'");
        weightedItemCollectionMap.put(key, weightedItemCollection);
    }

    public static WeightedItemCollection itemCollection(String key)
    {
        return weightedItemCollectionMap.get(key);
    }

    public static Set<String> allItemCollectionKeys()
    {
        return weightedItemCollectionMap.keySet();
    }

    public static void unregister(String key)
    {
        weightedItemCollectionMap.remove(key);
    }
}
