/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.item;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.world.storage.loot.InventoryGenerationHandler;
import ivorius.reccomplex.world.storage.loot.WeightedItemCollection;
import ivorius.reccomplex.world.storage.loot.WeightedItemCollectionRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public abstract class ItemInventoryGenerationTag extends Item implements GeneratingItem
{
    public ItemInventoryGenerationTag()
    {
    }

    public static boolean applyGeneratorToInventory(WorldServer world, BlockPos pos, GeneratingItem generatingItem, ItemStack stack)
    {
        TileEntity rightClicked = world.getTileEntity(pos);

        if (rightClicked instanceof IInventory)
        {
            if (!world.isRemote)
            {
                generatingItem.generateInInventory(world, (IInventory) rightClicked, world.rand, stack, world.rand.nextInt(((IInventory) rightClicked).getSizeInventory()));
                InventoryGenerationHandler.generateAllTags(world, (IInventory) rightClicked, RecurrentComplex.specialRegistry.itemHidingMode(), world.rand);
            }

            return true;
        }

        return false;
    }

    public static String inventoryGeneratorKey(ItemStack stack)
    {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("itemCollectionKey", Constants.NBT.TAG_STRING))
            return stack.getTagCompound().getString("itemCollectionKey");
        if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("display", Constants.NBT.TAG_COMPOUND)) // Legacy - Display Name
        {
            NBTTagCompound nbttagcompound = stack.getTagCompound().getCompoundTag("display");
            if (nbttagcompound.hasKey("Name", Constants.NBT.TAG_STRING))
                return nbttagcompound.getString("Name");
        }

        return null;
    }

    public static WeightedItemCollection inventoryGenerator(ItemStack stack)
    {
        return WeightedItemCollectionRegistry.INSTANCE.get(inventoryGeneratorKey(stack));
    }

    public static void setItemStackGeneratorKey(ItemStack stack, String generatorKey)
    {
        stack.setTagInfo("itemCollectionKey", new NBTTagString(generatorKey));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote)
            return applyGeneratorToInventory((WorldServer) worldIn, pos, this, player.getHeldItem(hand)) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, NonNullList<ItemStack> list)
    {
        for (String key : WeightedItemCollectionRegistry.INSTANCE.ids())
        {
            ItemStack stack = new ItemStack(item);
            setItemStackGeneratorKey(stack, key);
            list.add(stack);
        }
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public String getItemStackDisplayName(ItemStack stack)
    {
        String key = inventoryGeneratorKey(stack);

        return key != null ? key : super.getItemStackDisplayName(stack);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedInformation)
    {
        WeightedItemCollection generator = inventoryGenerator(stack);
        if (generator != null)
            list.add(generator.getDescriptor());
        else
            list.add(IvTranslations.get("inventoryGen.none"));
    }
}
