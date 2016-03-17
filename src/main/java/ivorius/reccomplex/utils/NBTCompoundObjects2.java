/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.collect.ImmutableMap;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.Map;

/**
 * Created by lukas on 01.03.16.
 */
public class NBTCompoundObjects2
{
    //TODO Merge into IvToolkit

    public static <K extends NBTCompoundObject, V extends NBTCompoundObject> Map<K, V> readMapFrom(NBTTagCompound compound, String key, Class<? extends K> keyClass, Class<? extends V> valueClass)
    {
        return readMap(compound.getTagList(key, Constants.NBT.TAG_COMPOUND), keyClass, valueClass);
    }

    public static <K extends NBTCompoundObject, V extends NBTCompoundObject> Map<K, V> readMap(NBTTagList nbt, Class<? extends K> keyClass, Class<? extends V> valueClass)
    {
        ImmutableMap.Builder<K, V> map = new ImmutableMap.Builder<>();
        for (int i = 0; i < nbt.tagCount(); i++)
        {
            NBTTagCompound compound = nbt.getCompoundTagAt(i);
            map.put(readFrom(compound, "key", keyClass), readFrom(compound, "value", valueClass));
        }
        return map.build();
    }

    public static <K extends NBTCompoundObject> K readFrom(NBTTagCompound compound, String key, Class<? extends K> keyClass)
    {
        return NBTCompoundObjects.read(compound.getCompoundTag(key), keyClass);
    }

    public static <K extends NBTCompoundObject, V extends NBTCompoundObject> void writeMapTo(NBTTagCompound compound, String key, Map<K, V> map)
    {
        compound.setTag(key, writeMap(map));
    }

    public static <K extends NBTCompoundObject, V extends NBTCompoundObject> NBTTagList writeMap(Map<K, V> map)
    {
        NBTTagList nbt = new NBTTagList();
        for (Map.Entry<K, V> entry : map.entrySet())
        {
            NBTTagCompound compound = new NBTTagCompound();
            writeTo(compound, "key", entry.getKey());
            writeTo(compound, "value", entry.getValue());
            nbt.appendTag(compound);
        }
        return nbt;
    }

    public static void writeTo(NBTTagCompound compound, String key, NBTCompoundObject compoundObject)
    {
        compound.setTag(key, NBTCompoundObjects.write(compoundObject));
    }
}
