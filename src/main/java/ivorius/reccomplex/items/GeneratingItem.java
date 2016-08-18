/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.items;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;

import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public interface GeneratingItem
{
    void generateInInventory(WorldServer server, IInventory inventory, Random random, ItemStack stack, int fromSlot);
}
