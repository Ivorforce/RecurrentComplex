/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.ivtoolkit.tools;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class IvInventoryHelper
{
    public static boolean consumeInventoryItem(InventoryPlayer inventory, ItemStack itemStack)
    {
        int var2 = getInventorySlotContainItem(inventory, itemStack);

        if (var2 < 0)
        {
            return false;
        }
        else
        {
            if (--inventory.mainInventory[var2].stackSize <= 0)
            {
                inventory.mainInventory[var2] = null;
            }

            return true;
        }
    }

    public static int getInventorySlotContainItem(InventoryPlayer inventory, ItemStack itemStack)
    {
        for (int var2 = 0; var2 < inventory.mainInventory.length; ++var2)
        {
            if (inventory.mainInventory[var2] != null && inventory.mainInventory[var2].isItemEqual(itemStack))
            {
                return var2;
            }
        }

        return -1;
    }

    public static int getInventorySlotContainItem(InventoryPlayer inventory, Item item)
    {
        for (int var2 = 0; var2 < inventory.mainInventory.length; ++var2)
        {
            if (inventory.mainInventory[var2] != null && inventory.mainInventory[var2].getItem() == item)
            {
                return var2;
            }
        }

        return -1;
    }
}
