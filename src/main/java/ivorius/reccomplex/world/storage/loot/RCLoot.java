/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.storage.loot;

import ivorius.reccomplex.files.loading.LeveledRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;

import java.util.Collection;

/**
 * Created by lukas on 07.06.14.
 */
public class RCLoot
{
    public static void registerVanillaLootTables()
    {
        registerVanillaLootTables(LootTableList.getAll());
    }

    private static void registerVanillaLootTables(Collection<ResourceLocation> keys)
    {
        for (ResourceLocation key : keys)
            WeightedItemCollectionRegistry.INSTANCE.register(key.toString(), "minecraft", new VanillaLootTable(key), true, LeveledRegistry.Level.INTERNAL);
    }
}
