/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editinventorygen;

import io.netty.buffer.ByteBuf;
import ivorius.reccomplex.gui.SlotDynamicIndex;
import ivorius.ivtoolkit.network.IGuiActionHandler;
import ivorius.reccomplex.worldgen.inventory.GenericInventoryGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 26.05.14.
 */
public class ContainerEditInventoryGen extends Container implements IGuiActionHandler
{
    public static final int ITEM_ROWS = 4;
    public static final int ITEM_COLUMNS = 1;
    public static final int SEGMENT_WIDTH = 288;

    public InventoryGenericInvGen_Single inventory;
    private GenericInventoryGenerator inventoryGenerator;

    private List<SlotDynamicIndex> scrollableSlots = new ArrayList<>();

    public ContainerEditInventoryGen(EntityPlayer player, GenericInventoryGenerator inventoryGenerator)
    {
        inventory = new InventoryGenericInvGen_Single(inventoryGenerator.weightedRandomChestContents);

        this.inventoryGenerator = inventoryGenerator;

        InventoryPlayer inventoryplayer = player.inventory;

        for (int col = 0; col < ITEM_COLUMNS; ++col)
        {
            for (int row = 0; row < ITEM_ROWS; ++row)
            {
                int index = col * ITEM_ROWS + row;
                SlotDynamicIndex slotLeft = new SlotDynamicIndex(inventory, index, col * SEGMENT_WIDTH, 50 + row * 18);

                this.addSlotToContainer(slotLeft);
                scrollableSlots.add(slotLeft);
            }
        }

        int basePlayerY = 50 + ITEM_ROWS * 18 + 13;
        int basePlayerX = (ITEM_COLUMNS * SEGMENT_WIDTH - 9 * 18) / 2;

        for (int row = 0; row < 3; ++row)
        {
            for (int col = 0; col < 9; ++col)
            {
                int index = col + row * 9 + 9;
                this.addSlotToContainer(new Slot(inventoryplayer, index, basePlayerX + col * 18, basePlayerY + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col)
        {
            int index = col;
            this.addSlotToContainer(new Slot(inventoryplayer, index, basePlayerX + col * 18, basePlayerY + 3 * 18 + 4));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer var1)
    {
        return true;
    }

    public void scrollTo(int colShift)
    {
//        List<WeightedRandomChestContent> items = inventoryGenerator.weightedRandomChestContents;

        for (int col = 0; col < ITEM_COLUMNS; ++col)
        {
            for (int row = 0; row < ITEM_ROWS; ++row)
            {
                int index = row + (col + colShift) * ITEM_ROWS;
                int scrollableSlotsIndex = row + col * ITEM_ROWS;

                scrollableSlots.get(scrollableSlotsIndex).slotIndex = index;
//                if (index >= 0 && index < items.size())
//                {
////                    inventory.setInventorySlotContents(col + row * 9, items.get(index).theItemId);
//                }
//                else
//                {
////                    inventory.setInventorySlotContents(col + row * 9, null);
//                }
            }
        }
    }

    @Override
    public void handleAction(String context, ByteBuf buffer)
    {
        if ("igSelectCol".equals(context))
        {
            scrollTo(buffer.readInt());
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            int generatorIndexMax = ITEM_COLUMNS * ITEM_ROWS;
            if (slotIndex < generatorIndexMax)
            {
                if (!this.mergeItemStack(itemstack1, generatorIndexMax, generatorIndexMax + 36, true))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 0, generatorIndexMax, false))
            {
                return null;
            }

            if (itemstack1.stackSize == 0)
            {
                slot.putStack((ItemStack)null);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize)
            {
                return null;
            }

            slot.onPickupFromSlot(player, itemstack1);
        }

        return itemstack;
    }
}
