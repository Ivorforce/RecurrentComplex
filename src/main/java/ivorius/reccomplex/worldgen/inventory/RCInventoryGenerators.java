/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;

import java.util.Collection;

/**
 * Created by lukas on 07.06.14.
 */
public class RCInventoryGenerators
{
    public static void registerVanillaInventoryGenerators()
    {
        registerVanillaInventoryGenerators(LootTableList.getAll());
    }

    private static void registerVanillaInventoryGenerators(Collection<ResourceLocation> keys)
    {
        for (ResourceLocation key : keys)
            WeightedItemCollectionRegistry.INSTANCE.register(key.toString(), "minecraft", new VanillaItemCollection(key), true, false);
    }
}
