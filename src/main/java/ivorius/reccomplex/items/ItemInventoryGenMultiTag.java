/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.items;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.inventorygen.GuiEditItemStack;
import ivorius.reccomplex.gui.inventorygen.TableDataSourceInvGenMultiTag;
import ivorius.reccomplex.utils.IvItemStacks;
import ivorius.reccomplex.worldgen.inventory.WeightedItemCollection;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ItemInventoryGenMultiTag extends ItemInventoryGenerationTag implements ItemSyncableTags
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
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if (worldIn.isRemote)
            openGui(playerIn, playerIn.inventory.currentItem);

        return super.onItemRightClick(itemStackIn, worldIn, playerIn, hand);
    }

    @SideOnly(Side.CLIENT)
    private void openGui(EntityPlayer player, int slot)
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditItemStack<>(player, slot, new TableDataSourceInvGenMultiTag()));
    }

    @Override
    public void generateInInventory(WorldServer server, IInventory inventory, Random random, ItemStack stack, int fromSlot)
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
                inventory.setInventorySlotContents(slot, weightedItemCollection.getRandomItemStack(server, random));
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedInformation)
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
    public List<Pair<String, Integer>> getSyncedNBTTags()
    {
        return Arrays.asList(Pair.of("itemCountMin", Constants.NBT.TAG_INT), Pair.of("itemCountMax", Constants.NBT.TAG_INT));
    }
}
