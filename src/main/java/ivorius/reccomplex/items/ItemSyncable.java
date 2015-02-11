/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.items;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by lukas on 11.02.15.
 */
public interface ItemSyncable
{
    void writeSyncedNBT(NBTTagCompound compound, ItemStack stack);

    void readSyncedNBT(NBTTagCompound compound, ItemStack stack);
}
