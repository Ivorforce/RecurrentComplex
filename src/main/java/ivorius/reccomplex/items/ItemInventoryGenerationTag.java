/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.items;

import ivorius.reccomplex.worldgen.inventory.InventoryGenerationHandler;
import ivorius.reccomplex.worldgen.inventory.WeightedItemCollectionRegistry;
import ivorius.reccomplex.worldgen.inventory.WeightedItemCollection;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

public abstract class ItemInventoryGenerationTag extends Item implements GeneratingItem
{
    public ItemInventoryGenerationTag()
    {
    }

    public static boolean applyGeneratorToInventory(World world, int x, int y, int z, GeneratingItem generatingItem, ItemStack stack)
    {
        TileEntity rightClicked = world.getTileEntity(x, y, z);

        if (rightClicked instanceof IInventory)
        {
            if (!world.isRemote)
            {
                generatingItem.generateInInventory((IInventory) rightClicked, world.rand, stack, world.rand.nextInt(((IInventory) rightClicked).getSizeInventory()));
                InventoryGenerationHandler.generateAllTags((IInventory) rightClicked, world.rand);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean onItemUse(ItemStack usedItem, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
    {
        return applyGeneratorToInventory(world, x, y, z, this, usedItem);
    }

    public static String inventoryGeneratorKey(ItemStack stack)
    {
        if (stack.hasDisplayName())
        {
            return stack.getDisplayName();
        }

        return null;
    }

    public static WeightedItemCollection inventoryGenerator(ItemStack stack)
    {
        return WeightedItemCollectionRegistry.itemCollection(inventoryGeneratorKey(stack));
    }

    @Override
    public void getSubItems(Item item, CreativeTabs creativeTabs, List list)
    {
        for (String key : WeightedItemCollectionRegistry.allItemCollectionKeys())
        {
            ItemStack stack = new ItemStack(item);
            setItemStackGeneratorKey(stack, key);
            list.add(stack);
        }
    }

    public static void setItemStackGeneratorKey(ItemStack stack, String generatorKey)
    {
        stack.setStackDisplayName(generatorKey);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advancedInformation)
    {
        WeightedItemCollection generator = inventoryGenerator(stack);
        if (generator != null)
            list.add(generator.getDescriptor());
        else
            list.add(StatCollector.translateToLocal("inventoryGen.none"));
    }
}
