/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import ivorius.reccomplex.items.GeneratingItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 05.01.15.
 */
public class InventoryGenerationHandler
{
    public static void generateAllTags(IInventory inventory, Random random)
    {
        List<Pair<ItemStack, Integer>> foundGenerators = new ArrayList<>();
        boolean didChange = true;
        int cycles = 0;

        do
        {
            if (didChange)
            {
                for (int i = 0; i < inventory.getSizeInventory(); i++)
                {
                    ItemStack stack = inventory.getStackInSlot(i);

                    if (stack != null && (stack.getItem() instanceof GeneratingItem))
                    {
                        foundGenerators.add(new ImmutablePair<>(stack, i));
                        inventory.setInventorySlotContents(i, null);
                    }
                }

                didChange = false;
            }

            if (foundGenerators.size() > 0)
            {
                Pair<ItemStack, Integer> pair = foundGenerators.get(0);
                ItemStack stack = pair.getLeft();
                ((GeneratingItem) stack.getItem()).generateInInventory(inventory, random, stack, pair.getRight());

                foundGenerators.remove(0);
                didChange = true;
            }

            cycles++;
        }
        while ((foundGenerators.size() > 0 || didChange) && cycles < 1000);
    }
}
