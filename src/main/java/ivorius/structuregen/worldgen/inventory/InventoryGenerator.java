/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.worldgen.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public interface InventoryGenerator
{
    void generateInInventory(Random random, IInventory inventory);

    void generateInInventorySlot(Random random, IInventory inventory, int slot);

    void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advancedInfo);

    boolean openEditGUI(ItemStack stack, EntityPlayer player, int slot);

    GenericInventoryGenerator copyAsGenericInventoryGenerator();

    boolean areDependenciesResolved();
}
