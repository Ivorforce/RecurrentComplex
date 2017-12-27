/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.item;

import ivorius.ivtoolkit.item.IvItemStacks;
import ivorius.reccomplex.gui.loot.GuiEditItemStack;
import ivorius.reccomplex.gui.loot.TableDataSourceLootGeneratorSingleTag;
import ivorius.reccomplex.world.storage.loot.LootTable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ItemLootGenSingleTag extends ItemLootGenerationTag implements ItemSyncableTags
{
    @Override
    public void generateInInventory(WorldServer server, IItemHandlerModifiable inventory, Random random, ItemStack stack, int fromSlot)
    {
        LootTable lootTable = lootTable(stack);

        if (lootTable != null)
        {
            ItemStack generated = random.nextFloat() < getItemChance(stack) ? lootTable.getRandomItemStack(server, random) : null;

            if (generated != null)
                inventory.setStackInSlot(fromSlot, generated);
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if (worldIn.isRemote)
            //noinspection MethodCallSideOnly
            openGui(playerIn, playerIn.inventory.currentItem);

        return super.onItemRightClick(worldIn, playerIn, hand);
    }

    @SideOnly(Side.CLIENT)
    private void openGui(EntityPlayer player, int slot)
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditItemStack<>(player, slot, new TableDataSourceLootGeneratorSingleTag()));
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
