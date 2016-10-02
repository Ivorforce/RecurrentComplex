/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.items;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by lukas on 11.02.15.
 */
public interface ItemSyncableTags extends ItemSyncable
{
    @Override
    default void writeSyncedNBT(NBTTagCompound compound, ItemStack stack)
    {
        getSyncedNBTTags().forEach(pair ->
        {
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey(pair.getLeft(), pair.getRight()))
                compound.setTag(pair.getLeft(), stack.getTagCompound().getTag(pair.getLeft()));
        });
    }

    @Override
    default void readSyncedNBT(NBTTagCompound compound, ItemStack stack)
    {
        getSyncedNBTTags().forEach(pair ->
        {
            if (compound.hasKey(pair.getLeft(), pair.getRight()))
                stack.setTagInfo(pair.getLeft(), compound.getTag(pair.getLeft()));
        });
    }

    List<Pair<String, Integer>> getSyncedNBTTags();
}
