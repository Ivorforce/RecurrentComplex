/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.items.GeneratingItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Created by lukas on 25.05.14.
 */
public class InventoryGeneratorRegistry
{
    private static Map<String, WeightedItemCollection> inventoryGeneratorMap = new HashMap<>();

    public static void registerInventoryGenerator(WeightedItemCollection weightedItemCollection, String key)
    {
        RecurrentComplex.logger.info(inventoryGeneratorMap.containsKey(key) ? "Overwrote inventory generator with id '" + key + "'" : "Registered inventory generator with id '" + key + "'");
        inventoryGeneratorMap.put(key, weightedItemCollection);
    }

    public static WeightedItemCollection generator(String key)
    {
        return inventoryGeneratorMap.get(key);
    }

    public static Set<String> allInventoryGeneratorKeys()
    {
        return inventoryGeneratorMap.keySet();
    }

    public static void removeGenerator(String key)
    {
        inventoryGeneratorMap.remove(key);
    }
}
