/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.storage.loot;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.item.GeneratingItem;
import ivorius.reccomplex.world.gen.feature.structure.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.registry.MCRegistrySpecial;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 05.01.15.
 */
public class InventoryGenerationHandler
{
    public static void generateAllTags(WorldServer server, IInventory inventory, MCRegistrySpecial.ItemHidingRegistry registry, Random random)
    {
        List<Triple<ItemStack, GeneratingItem, Integer>> foundGenerators = new ArrayList<>();
        boolean didChange = true;
        int cycles = 0;

        do
        {
            if (didChange)
            {
                for (int i = 0; i < inventory.getSizeInventory(); i++)
                {
                    ItemStack stack = inventory.getStackInSlot(i);

                    if (stack != null)
                    {
                        Item item = registry.containedItem(stack);
                        if (item instanceof GeneratingItem)
                        {
                            foundGenerators.add(Triple.of(stack, (GeneratingItem) item, i));
                            inventory.setInventorySlotContents(i, null);
                        }
                    }
                }

                didChange = false;
            }

            if (foundGenerators.size() > 0)
            {
                Triple<ItemStack, GeneratingItem, Integer> pair = foundGenerators.get(0);
                pair.getMiddle().generateInInventory(server, inventory, random, pair.getLeft(), pair.getRight());

                foundGenerators.remove(0);
                didChange = true;
            }

            cycles++;
        }
        while ((foundGenerators.size() > 0 || didChange) && cycles < 1000);
    }

    public static void generateAllTags(@Nonnull StructureSpawnContext context, IInventory inventory)
    {
        generateAllTags(context.environment.world, inventory, RecurrentComplex.specialRegistry.itemHidingMode(), context.random);
    }
}
