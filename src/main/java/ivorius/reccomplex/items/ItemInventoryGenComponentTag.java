/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.items;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.RCGuiHandler;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollectionRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 05.01.15.
 */
public class ItemInventoryGenComponentTag extends Item implements GeneratingItem
{
    public static String componentKey(ItemStack stack)
    {
        if (stack.hasDisplayName())
            return stack.getDisplayName();

        return null;
    }

    public static void setComponentKey(ItemStack stack, String generatorKey)
    {
        stack.setStackDisplayName(generatorKey);
    }

    public static Component component(ItemStack stack)
    {
        return GenericItemCollectionRegistry.component(componentKey(stack));
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        if (component(stack) != null || componentKey(stack) == null)
        {
            if (!world.isRemote)
                player.openGui(RecurrentComplex.instance, RCGuiHandler.editInventoryGen, world, player.inventory.currentItem, 0, 0);
        }

        return super.onItemRightClick(stack, world, player);
    }

    @Override
    public void generateInInventory(IInventory inventory, Random random, ItemStack stack, int fromSlot)
    {
        Component component = component(stack);

        if (component != null)
            inventory.setInventorySlotContents(fromSlot, component.getRandomItemStack(random));
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advancedInformation)
    {
        super.addInformation(stack, player, list, advancedInformation);

        Component component = component(stack);

        if (component != null)
            list.add(component.inventoryGeneratorID);
        else
            list.add(StatCollector.translateToLocal("inventoryGen.create"));
    }

    @Override
    public void getSubItems(Item item, CreativeTabs creativeTabs, List list)
    {
        super.getSubItems(item, creativeTabs, list);

        for (String key : GenericItemCollectionRegistry.allComponentKeys())
        {
            ItemStack stack = new ItemStack(item);
            setComponentKey(stack, key);
            list.add(stack);
        }
    }
}
