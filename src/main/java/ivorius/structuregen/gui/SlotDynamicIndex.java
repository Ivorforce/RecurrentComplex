package ivorius.structuregen.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Created by lukas on 29.05.14.
 */
public class SlotDynamicIndex extends Slot
{
    public int slotIndex;

    public SlotDynamicIndex(IInventory inventory, int slotIndex, int x, int y)
    {
        super(inventory, slotIndex, x, y);

        this.slotIndex = slotIndex;
    }

    @Override
    public ItemStack getStack()
    {
        return this.inventory.getStackInSlot(this.slotIndex);
    }

    @Override
    public void putStack(ItemStack par1ItemStack)
    {
        this.inventory.setInventorySlotContents(this.slotIndex, par1ItemStack);
        this.onSlotChanged();
    }

    @Override
    public ItemStack decrStackSize(int par1)
    {
        return this.inventory.decrStackSize(this.slotIndex, par1);
    }

    @Override
    public boolean isSlotInInventory(IInventory par1IInventory, int par2)
    {
        return par1IInventory == this.inventory && par2 == this.slotIndex;
    }

    @Override
    public int getSlotIndex()
    {
        return slotIndex;
    }
}
