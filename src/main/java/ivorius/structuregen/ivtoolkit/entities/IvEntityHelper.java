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

package ivorius.structuregen.ivtoolkit.entities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class IvEntityHelper
{
    public static boolean addAsCurrentItem(EntityPlayer player, ItemStack stack)
    {
        return addAsCurrentItem(player.inventory, stack, player.worldObj.isRemote);
    }

    public static boolean addAsCurrentItem(InventoryPlayer inventory, ItemStack stack, boolean isRemote)
    {
        int var6;

        if (inventory.getStackInSlot(inventory.currentItem) != null)
        {
            var6 = inventory.getFirstEmptyStack();
        }
        else
        {
            var6 = inventory.currentItem;
        }

        if (var6 >= 0 && var6 < 9)
        {
            inventory.currentItem = var6;

            if (!isRemote)
            {
                inventory.setInventorySlotContents(inventory.currentItem, stack);
            }

            return true;
        }

        return false;
    }
}
