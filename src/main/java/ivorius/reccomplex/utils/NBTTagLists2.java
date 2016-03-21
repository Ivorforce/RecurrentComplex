/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.List;

/**
 * Created by lukas on 29.02.16.
 */
public class NBTTagLists2
{
    //TODO Merge into IvToolkit

    public static List<NBTBase> nbtBases(NBTTagList nbt)
    {
        ImmutableList.Builder<NBTBase> list = new ImmutableList.Builder<>();
        NBTTagList copy = (NBTTagList) nbt.copy(); // TODO Change to getTagListAt when available

        while (copy.tagCount() > 0)
            list.add(copy.removeTag(0));

        return list.build();
    }

    public static List<NBTTagList> listsFrom(NBTTagCompound compound, String key)
    {
        return lists(compound.getTagList(key, Constants.NBT.TAG_LIST));
    }

    public static List<NBTTagList> lists(NBTTagList nbt)
    {
        if (nbt.func_150303_d() != Constants.NBT.TAG_LIST)
            throw new IllegalArgumentException();

        ImmutableList.Builder<NBTTagList> list = new ImmutableList.Builder<>();
        NBTTagList copy = (NBTTagList) nbt.copy(); // TODO Change to getTagListAt when available

        while (copy.tagCount() > 0)
            list.add((NBTTagList) copy.removeTag(0));

        return list.build();
    }

    public static void writeNbt(NBTTagCompound compound, String key, List<NBTTagList> lists)
    {
        compound.setTag(key, nbt(lists));
    }

    public static NBTTagList nbt(List<NBTTagList> lists)
    {
        NBTTagList list = new NBTTagList();
        lists.forEach(list::appendTag);
        return list;
    }
}
