/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.util.List;

/**
 * Created by lukas on 07.06.14.
 */
public abstract class ModInventoryGenerator implements InventoryGenerator
{
    private String modName;

    protected ModInventoryGenerator(String modName)
    {
        this.modName = modName;
    }

    public String getModName()
    {
        return modName;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advancedInfo)
    {
        String modName = getModName();
        list.add(StatCollector.translateToLocal("inventoryGen.mod") + (modName != null ? (": " + getModName()) : ""));
    }

    @Override
    public boolean openEditGUI(ItemStack stack, EntityPlayer player, int slot)
    {
        return false;
    }
}
