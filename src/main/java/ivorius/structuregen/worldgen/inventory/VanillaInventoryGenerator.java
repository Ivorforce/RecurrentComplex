/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.worldgen.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class VanillaInventoryGenerator implements InventoryGenerator
{
    public String vanillaKey;

    public VanillaInventoryGenerator(String vanillaKey)
    {
        this.vanillaKey = vanillaKey;
    }

    @Override
    public void generateInInventory(Random random, IInventory inventory)
    {
        ChestGenHooks info = ChestGenHooks.getInfo(vanillaKey);
        WeightedRandomChestContent.generateChestContents(random, info.getItems(random), inventory, info.getCount(random));
    }

    @Override
    public void generateInInventorySlot(Random random, IInventory inventory, int slot)
    {
        inventory.setInventorySlotContents(slot, ChestGenHooks.getOneItem(vanillaKey, random));
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advancedInfo)
    {
        list.add(StatCollector.translateToLocal("inventoryGen.vanilla"));
    }

    @Override
    public boolean openEditGUI(ItemStack stack, EntityPlayer player, int slot)
    {
        return false;
    }

    @Override
    public GenericInventoryGenerator copyAsGenericInventoryGenerator()
    {
        return null;
    }

    @Override
    public boolean areDependenciesResolved()
    {
        return true;
    }
}
