/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.storage.loot;

import net.minecraft.item.ItemStack;

/**
 * Do not use - just for legacy
 */
public class WeightedRandomChestContent
{
    public ItemStack theItemId;
    public int minStackSize;
    public int maxStackSize;
    public int itemWeight;

    public WeightedRandomChestContent(ItemStack theItemId, int minStackSize, int maxStackSize, int itemWeight)
    {
        this.theItemId = theItemId;
        this.minStackSize = minStackSize;
        this.maxStackSize = maxStackSize;
        this.itemWeight = itemWeight;
    }
}
