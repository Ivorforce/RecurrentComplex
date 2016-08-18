/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class VanillaItemCollection implements WeightedItemCollection
{
    public ResourceLocation vanillaKey;

    public VanillaItemCollection(ResourceLocation lootTableKey)
    {
        this.vanillaKey = lootTableKey;
    }

    @Override
    public ItemStack getRandomItemStack(WorldServer server, Random random)
    {
        LootTable loottable = server.getLootTableManager().getLootTableFromLocation(this.vanillaKey);
        List<ItemStack> loot = loottable.generateLootForPools(random, new LootContext.Builder(server).build());
        return loot.size() > 0 ? loot.get(0) : null; // TODO generate tile entities with loot? TileEntityLockedLoot
    }

    @Override
    public String getDescriptor()
    {
        return I18n.translateToLocal("inventoryGen.vanilla");
    }
}
