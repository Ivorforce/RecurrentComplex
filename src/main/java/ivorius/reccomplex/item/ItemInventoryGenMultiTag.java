/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.item;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.item.IvItemStacks;
import ivorius.reccomplex.gui.inventorygen.GuiEditItemStack;
import ivorius.reccomplex.gui.inventorygen.TableDataSourceInvGenMultiTag;
import ivorius.reccomplex.world.storage.loot.WeightedItemCollection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ItemInventoryGenMultiTag extends ItemInventoryGenerationTag implements ItemSyncableTags
{
    public static TIntList emptySlots(IItemHandler inv)
    {
        TIntList list = new TIntArrayList();
        for (int i = 0; i < inv.getSlots(); i++)
        {
            if (inv.getStackInSlot(i) == ItemStack.EMPTY)
                list.add(i);
        }
        return list;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        if (worldIn.isRemote)
            //noinspection MethodCallSideOnly
            openGui(playerIn, playerIn.inventory.currentItem);

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @SideOnly(Side.CLIENT)
    private void openGui(EntityPlayer player, int slot)
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditItemStack<>(player, slot, new TableDataSourceInvGenMultiTag()));
    }

    @Override
    public void generateInInventory(WorldServer server, IItemHandlerModifiable inventory, Random random, ItemStack stack, int fromSlot)
    {
        WeightedItemCollection weightedItemCollection = inventoryGenerator(stack);

        inventory.setStackInSlot(fromSlot, ItemStack.EMPTY);

        if (weightedItemCollection != null)
        {
            IntegerRange range = getGenerationCount(stack);
            int amount = range.getMin() < range.getMax() ? random.nextInt(range.getMax() - range.getMin() + 1) + range.getMin() : 0;

            TIntList emptySlots = emptySlots(inventory);

            for (int i = 0; i < amount; i++)
            {
                int slot = emptySlots.isEmpty()
                        ? random.nextInt(inventory.getSlots())
                        : emptySlots.removeAt(random.nextInt(emptySlots.size()));

                ItemStack generated = weightedItemCollection.getRandomItemStack(server, random);

                if (generated != null)
                    inventory.setStackInSlot(slot, generated);
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        IntegerRange range = getGenerationCount(stack);
        tooltip.add(String.format("%d - %d Items", range.getMin(), range.getMax()));
    }

    public IntegerRange getGenerationCount(ItemStack stack)
    {
        return new IntegerRange(IvItemStacks.getNBT(stack, "itemCountMin", 4),
                IvItemStacks.getNBT(stack, "itemCountMax", 8));
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
