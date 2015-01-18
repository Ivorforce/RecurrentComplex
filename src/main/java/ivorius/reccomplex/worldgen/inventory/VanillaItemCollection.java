/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.ChestGenHooks;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class VanillaItemCollection implements WeightedItemCollection
{
    public String vanillaKey;

    public VanillaItemCollection(String vanillaKey)
    {
        this.vanillaKey = vanillaKey;
    }

    @Override
    public ItemStack getRandomItemStack(Random random)
    {
        return ChestGenHooks.getOneItem(vanillaKey, random);
    }

    @Override
    public String getDescriptor()
    {
        return StatCollector.translateToLocal("inventoryGen.vanilla");
    }
}
