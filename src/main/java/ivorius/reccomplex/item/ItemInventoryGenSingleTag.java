/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.item;

import ivorius.ivtoolkit.item.IvItemStacks;
import ivorius.reccomplex.gui.inventorygen.GuiEditItemStack;
import ivorius.reccomplex.gui.inventorygen.TableDataSourceInvGenSingleTag;
import ivorius.reccomplex.world.storage.loot.WeightedItemCollection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ItemInventoryGenSingleTag extends ItemInventoryGenerationTag implements ItemSyncableTags
{
    @Override
    public void generateInInventory(WorldServer server, IInventory inventory, Random random, ItemStack stack, int fromSlot)
    {
        WeightedItemCollection weightedItemCollection = inventoryGenerator(stack);

        if (weightedItemCollection != null)
        {
            ItemStack generated = random.nextFloat() < getItemChance(stack) ? weightedItemCollection.getRandomItemStack(server, random) : null;

            if (generated != null)
                inventory.setInventorySlotContents(fromSlot, generated);
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if (worldIn.isRemote)
            openGui(playerIn, playerIn.inventory.currentItem);

        return super.onItemRightClick(worldIn, playerIn, hand);
    }

    @SideOnly(Side.CLIENT)
    private void openGui(EntityPlayer player, int slot)
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditItemStack<>(player, slot, new TableDataSourceInvGenSingleTag()));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(String.format("%f Chance", getItemChance(stack)));
    }

    public float getItemChance(ItemStack stack)
    {
        return IvItemStacks.getNBT(stack, "itemChance", 1f);
    }

    public void setItemChance(ItemStack stack, float chance)
    {
        stack.setTagInfo("itemChance", new NBTTagFloat(chance));
    }

    @Override
    public List<Pair<String, Integer>> getSyncedNBTTags()
    {
        return Collections.singletonList(Pair.of("itemChance", Constants.NBT.TAG_FLOAT));
    }
}
