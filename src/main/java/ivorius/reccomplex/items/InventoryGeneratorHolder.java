/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.items;

import net.minecraft.item.ItemStack;

/**
 * Created by lukas on 26.05.14.
 */
public interface InventoryGeneratorHolder
{
    String inventoryKey(ItemStack stack);
}
