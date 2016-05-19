/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editinventorygen;

import ivorius.reccomplex.gui.InventoryWatcher;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 27.05.14.
 */
public class InventoryGenericInvGen_Single implements IInventory
{
    public List<GenericItemCollection.RandomizedItemStack> weightedRandomChestContents;

    private List<InventoryWatcher> watchers = new ArrayList<>();

    public InventoryGenericInvGen_Single(List<GenericItemCollection.RandomizedItemStack> weightedRandomChestContents)
    {
        this.weightedRandomChestContents = weightedRandomChestContents;
    }

    public void addWatcher(InventoryWatcher watcher)
    {
        watchers.add(watcher);
    }

    public void removeWatcher(InventoryWatcher watcher)
    {
        watchers.remove(watcher);
    }

    public List<InventoryWatcher> watchers()
    {
        return Collections.unmodifiableList(watchers);
    }

    @Override
    public int getSizeInventory()
    {
        return weightedRandomChestContents.size() + 1;
    }

    @Override
    public ItemStack getStackInSlot(int var1)
    {
        return var1 < weightedRandomChestContents.size() ? weightedRandomChestContents.get(var1).itemStack : null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int dec)
    {
        if (slot < weightedRandomChestContents.size())
        {
            ItemStack stack = weightedRandomChestContents.get(slot).itemStack;
            if (stack != null)
            {
                ItemStack itemstack;

                if (stack.stackSize <= dec)
                {
                    itemstack = stack;
                    weightedRandomChestContents.remove(slot);
                    this.markDirty();
                    return itemstack;
                }
                else
                {
                    itemstack = stack.splitStack(dec);

                    if (stack.stackSize == 0)
                    {
                        weightedRandomChestContents.remove(slot);
                    }

                    this.markDirty();
                    return itemstack;
                }
            }
        }

        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int var1)
    {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        if (slot < weightedRandomChestContents.size())
        {
            if (stack != null)
                weightedRandomChestContents.get(slot).itemStack = stack;
            else
                weightedRandomChestContents.remove(slot);
        }
        else
        {
            if (stack != null)
                weightedRandomChestContents.add(new GenericItemCollection.RandomizedItemStack(stack, 1, stack.getMaxStackSize(), 1.0));
        }

        if (stack != null && stack.stackSize > this.getInventoryStackLimit())
        {
            stack.stackSize = this.getInventoryStackLimit();
        }

        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    @Override
    public void markDirty()
    {
        for (InventoryWatcher watcher : this.watchers)
            watcher.inventoryChanged(this);
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer var1)
    {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {

    }

    @Override
    public void closeInventory(EntityPlayer player)
    {

    }

    @Override
    public boolean isItemValidForSlot(int var1, ItemStack var2)
    {
        return true;
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {

    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public void clear()
    {

    }

    @Override
    public String getCommandSenderName()
    {
        return null;
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public IChatComponent getDisplayName()
    {
        return null;
    }
}
