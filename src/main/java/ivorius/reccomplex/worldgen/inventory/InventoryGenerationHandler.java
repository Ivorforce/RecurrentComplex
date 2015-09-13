/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import ivorius.reccomplex.items.InventoryScript;
import ivorius.reccomplex.structures.MCRegistrySpecial;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 05.01.15.
 */
public class InventoryGenerationHandler
{
    public static void generateAllTags(IInventory inventory, MCRegistrySpecial.ItemHidingRegistry registry, Random random)
    {
        List<Triple<ItemStack, InventoryScript, Integer>> foundGenerators = new ArrayList<>();
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
                        if (item instanceof InventoryScript)
                        {
                            foundGenerators.add(Triple.of(stack, (InventoryScript) item, i));
                            inventory.setInventorySlotContents(i, null);
                        }
                    }
                }

                didChange = false;
            }

            if (foundGenerators.size() > 0)
            {
                Triple<ItemStack, InventoryScript, Integer> pair = foundGenerators.get(0);
                pair.getMiddle().generateInInventory(inventory, random, pair.getLeft(), pair.getRight());

                foundGenerators.remove(0);
                didChange = true;
            }

            cycles++;
        }
        while ((foundGenerators.size() > 0 || didChange) && cycles < 1000);
    }
}
