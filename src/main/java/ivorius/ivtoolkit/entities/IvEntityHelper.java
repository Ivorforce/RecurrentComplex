/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.ivtoolkit.entities;

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
