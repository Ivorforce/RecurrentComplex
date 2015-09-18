/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.items;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.RCGuiHandler;
import ivorius.reccomplex.utils.IvTranslations;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollectionRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 05.01.15.
 */
public class ItemInventoryGenComponentTag extends Item implements GeneratingItem
{
    public static String componentKey(ItemStack stack)
    {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("itemCollectionKey", Constants.NBT.TAG_STRING))
            return stack.getTagCompound().getString("itemCollectionKey");
        if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("display", Constants.NBT.TAG_COMPOUND)) // Legacy - Display Name
        {
            NBTTagCompound nbttagcompound = stack.stackTagCompound.getCompoundTag("display");
            if (nbttagcompound.hasKey("Name", Constants.NBT.TAG_STRING))
                return nbttagcompound.getString("Name");
        }


        return null;
    }

    public static void setComponentKey(ItemStack stack, String generatorKey)
    {
        stack.setTagInfo("itemCollectionKey", new NBTTagString(generatorKey));
    }

    public static Component component(ItemStack stack)
    {
        return GenericItemCollectionRegistry.INSTANCE.component(componentKey(stack));
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
    public String getItemStackDisplayName(ItemStack stack)
    {
        String key = componentKey(stack);

        return key != null ? key : super.getItemStackDisplayName(stack);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advancedInformation)
    {
        super.addInformation(stack, player, list, advancedInformation);

        Component component = component(stack);

        if (component != null)
        {
            list.add(component.inventoryGeneratorID);
            list.add(GenericItemCollectionRegistry.INSTANCE.isActive(component.inventoryGeneratorID)
                    ? IvTranslations.format("inventoryGen.active", EnumChatFormatting.GREEN, EnumChatFormatting.RESET)
                    : IvTranslations.format("inventoryGen.inactive", EnumChatFormatting.RED, EnumChatFormatting.RESET));
        }
        else
            list.add(IvTranslations.get("inventoryGen.create"));
    }

    @Override
    public void getSubItems(Item item, CreativeTabs creativeTabs, List list)
    {
        super.getSubItems(item, creativeTabs, list);

        for (String key : GenericItemCollectionRegistry.INSTANCE.allComponentKeys())
        {
            ItemStack stack = new ItemStack(item);
            setComponentKey(stack, key);
            list.add(stack);
        }
    }
}
