/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.items;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public interface InventoryScript
{
    void generateInInventory(IInventory inventory, Random random, ItemStack stack, int fromSlot);
}
