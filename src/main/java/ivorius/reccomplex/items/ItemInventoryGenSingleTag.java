/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.items;

import ivorius.reccomplex.worldgen.inventory.WeightedItemCollection;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;

import java.util.Random;

public class ItemInventoryGenSingleTag extends ItemInventoryGenerationTag
{
    @Override
    public void generateInInventory(WorldServer server, IInventory inventory, Random random, ItemStack stack, int fromSlot)
    {
        WeightedItemCollection weightedItemCollection = inventoryGenerator(stack);

        if (weightedItemCollection != null)
            inventory.setInventorySlotContents(fromSlot, weightedItemCollection.getRandomItemStack(server, random));
    }
}
