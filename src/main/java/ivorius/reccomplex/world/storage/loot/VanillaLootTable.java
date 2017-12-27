/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.storage.loot;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class VanillaLootTable implements LootTable
{
    public ResourceLocation vanillaKey;

    public VanillaLootTable(ResourceLocation lootTableKey)
    {
        this.vanillaKey = lootTableKey;
    }

    @Override
    public ItemStack getRandomItemStack(WorldServer server, Random random)
    {
        List<ItemStack> loot = null;

        try
        {
            net.minecraft.world.storage.loot.LootTable loottable = server.getLootTableManager().getLootTableFromLocation(this.vanillaKey);
            loot = loottable.generateLootForPools(random, new LootContext.Builder(server).build());
            return loot.size() > 0 ? loot.get(0) : null; // TODO generate tile entities with loot? TileEntityLockedLoot
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.error("Error generating vanilla loot", e);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public String getDescriptor()
    {
        return IvTranslations.get("inventoryGen.vanilla");
    }
}
