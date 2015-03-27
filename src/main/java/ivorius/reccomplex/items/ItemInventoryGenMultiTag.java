/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.inventorygen.GuiEditInvGenMultiTag;
import ivorius.reccomplex.utils.IvItemStacks;
import ivorius.reccomplex.worldgen.inventory.WeightedItemCollection;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class ItemInventoryGenMultiTag extends ItemInventoryGenerationTag implements ItemSyncable
{
    public static TIntList emptySlots(IInventory inv)
    {
        TIntList list = new TIntArrayList();
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            if (inv.getStackInSlot(i) == null)
                list.add(i);
        }
        return list;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        if (world.isRemote)
            openGui(player, player.inventory.currentItem);

        return super.onItemRightClick(stack, world, player);
    }

    @SideOnly(Side.CLIENT)
    private void openGui(EntityPlayer player, int slot)
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditInvGenMultiTag(player, slot));
    }

    @Override
    public void generateInInventory(IInventory inventory, Random random, ItemStack stack, int fromSlot)
    {
        WeightedItemCollection weightedItemCollection = inventoryGenerator(stack);

        inventory.setInventorySlotContents(fromSlot, null);

        if (weightedItemCollection != null)
        {
            IntegerRange range = getGenerationCount(stack);
            int amount = range.getMin() < range.getMax() ? random.nextInt(range.getMax() - range.getMin() + 1) + range.getMin() : 0;

            TIntList emptySlots = emptySlots(inventory);

            for (int i = 0; i < amount; i++)
            {
                int slot = emptySlots.isEmpty()
                        ? random.nextInt(inventory.getSizeInventory())
                        : emptySlots.removeAt(random.nextInt(emptySlots.size()));
                inventory.setInventorySlotContents(slot, weightedItemCollection.getRandomItemStack(random));
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advancedInformation)
    {
        super.addInformation(stack, player, list, advancedInformation);
        IntegerRange range = getGenerationCount(stack);
        list.add(String.format("%d - %d Items", range.getMin(), range.getMax()));
    }

    public IntegerRange getGenerationCount(ItemStack stack)
    {
        return new IntegerRange(IvItemStacks.getNBTInt(stack, "itemCountMin", 4),
                IvItemStacks.getNBTInt(stack, "itemCountMax", 8));
    }

    public void setGenerationCount(ItemStack stack, IntegerRange range)
    {
        stack.setTagInfo("itemCountMin", new NBTTagInt(range.getMin()));
        stack.setTagInfo("itemCountMax", new NBTTagInt(range.getMax()));
    }

    @Override
    public void writeSyncedNBT(NBTTagCompound compound, ItemStack stack)
    {
        IntegerRange range = getGenerationCount(stack);
        compound.setInteger("itemCountMin", range.getMin());
        compound.setInteger("itemCountMax", range.getMax());
    }

    @Override
    public void readSyncedNBT(NBTTagCompound compound, ItemStack stack)
    {
        setGenerationCount(stack, new IntegerRange(compound.getInteger("itemCountMin"), compound.getInteger("itemCountMax")));
    }
}
