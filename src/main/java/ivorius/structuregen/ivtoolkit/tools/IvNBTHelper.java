/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.structuregen.ivtoolkit.tools;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.util.Constants;

/**
 * Created by lukas on 23.04.14.
 */
public class IvNBTHelper
{
    public static double[] readDoubleArray(String key, NBTTagCompound compound)
    {
        if (compound.hasKey(key))
        {
            NBTTagList list = compound.getTagList(key, Constants.NBT.TAG_DOUBLE);
            double[] array = new double[list.tagCount()];

            for (int i = 0; i < array.length; i++)
            {
                array[i] = list.func_150309_d(i);
            }

            return array;
        }

        return null;
    }

    public static void writeDoubleArray(String key, double[] array, NBTTagCompound compound)
    {
        if (array != null)
        {
            NBTTagList list = new NBTTagList();

            for (double d : array)
            {
                list.appendTag(new NBTTagDouble(d));
            }

            compound.setTag(key, list);
        }
    }

    public static String[] readNBTStrings(String id, NBTTagCompound compound)
    {
        if (compound.hasKey(id))
        {
            NBTTagList nbtTagList = compound.getTagList(id, Constants.NBT.TAG_STRING);
            String[] strings = new String[nbtTagList.tagCount()];

            for (int i = 0; i < strings.length; i++)
            {
                strings[i] = nbtTagList.getStringTagAt(i);
            }

            return strings;
        }

        return null;
    }

    public static void writeNBTStrings(String id, String[] strings, NBTTagCompound compound)
    {
        if (strings != null)
        {
            NBTTagList nbtTagList = new NBTTagList();

            for (String s : strings)
            {
                nbtTagList.appendTag(new NBTTagString(s));
            }

            compound.setTag(id, nbtTagList);
        }
    }

    public static String[][] readNBTStrings2D(String id, NBTTagCompound compound)
    {
        if (compound.hasKey(id))
        {
            NBTTagList nbtTagList = compound.getTagList(id, Constants.NBT.TAG_COMPOUND);
            String[][] strings = new String[nbtTagList.tagCount()][];

            for (int i = 0; i < strings.length; i++)
            {
                strings[i] = readNBTStrings("Strings", nbtTagList.getCompoundTagAt(i));
            }

            return strings;
        }

        return null;
    }

    public static void writeNBTStrings2D(String id, String[][] strings, NBTTagCompound compound)
    {
        if (strings != null)
        {
            NBTTagList nbtTagList = new NBTTagList();

            for (String[] s : strings)
            {
                NBTTagCompound compound1 = new NBTTagCompound();
                writeNBTStrings("Strings", s, compound1);
                nbtTagList.appendTag(compound1);
            }

            compound.setTag(id, nbtTagList);
        }
    }

    public static ItemStack[] readNBTStacks(String id, NBTTagCompound compound)
    {
        if (compound.hasKey(id))
        {
            NBTTagList nbtTagList = compound.getTagList(id, Constants.NBT.TAG_COMPOUND);
            ItemStack[] itemStacks = new ItemStack[nbtTagList.tagCount()];

            for (int i = 0; i < itemStacks.length; i++)
            {
                itemStacks[i] = ItemStack.loadItemStackFromNBT(nbtTagList.getCompoundTagAt(i));
            }

            return itemStacks;
        }

        return null;
    }

    public static void writeNBTStacks(String id, ItemStack[] stacks, NBTTagCompound compound)
    {
        if (stacks != null)
        {
            NBTTagList nbtTagList = new NBTTagList();

            for (ItemStack stack : stacks)
            {
                NBTTagCompound tagCompound = new NBTTagCompound();
                stack.writeToNBT(tagCompound);
                nbtTagList.appendTag(tagCompound);
            }

            compound.setTag(id, nbtTagList);
        }
    }

    public static Block[] readNBTBlocks(String id, NBTTagCompound compound)
    {
        if (compound.hasKey(id))
        {
            NBTTagList nbtTagList = compound.getTagList(id, Constants.NBT.TAG_STRING);
            Block[] blocks = new Block[nbtTagList.tagCount()];

            for (int i = 0; i < blocks.length; i++)
            {
                blocks[i] = (Block) Block.blockRegistry.getObject(nbtTagList.getStringTagAt(i));
            }

            return blocks;
        }

        return null;
    }

    public static void writeNBTBlocks(String id, Block[] blocks, NBTTagCompound compound)
    {
        if (blocks != null)
        {
            NBTTagList nbtTagList = new NBTTagList();

            for (Block b : blocks)
            {
                nbtTagList.appendTag(new NBTTagString(Block.blockRegistry.getNameForObject(b)));
            }

            compound.setTag(id, nbtTagList);
        }
    }

    public static long[] readNBTLongs(String id, NBTTagCompound compound)
    {
        if (compound.hasKey(id))
        {
            NBTTagList nbtTagList = compound.getTagList(id, Constants.NBT.TAG_INT);
            long[] longs = new long[nbtTagList.tagCount()];

            for (int i = 0; i < longs.length; i++)
            {
                int[] parts = nbtTagList.func_150306_c(i);
                longs[i] = (long) parts[0] + ((long) parts[1] << 32);
            }

            return longs;
        }

        return null;
    }

    public static void writeNBTLongs(String id, long[] longs, NBTTagCompound compound)
    {
        if (longs != null)
        {
            NBTTagList nbtTagList = new NBTTagList();

            for (long l : longs)
            {
                nbtTagList.appendTag(new NBTTagIntArray(new int[]{(int) l, (int) (l >>> 32)}));
            }

            compound.setTag(id, nbtTagList);
        }
    }

    public static PotionEffect[] readNBTPotions(String id, NBTTagCompound compound)
    {
        if (compound.hasKey(id))
        {
            NBTTagList nbtTagList = compound.getTagList(id, Constants.NBT.TAG_STRING);
            PotionEffect[] potions = new PotionEffect[nbtTagList.tagCount()];

            for (int i = 0; i < potions.length; i++)
            {
                potions[i] = PotionEffect.readCustomPotionEffectFromNBT(nbtTagList.getCompoundTagAt(i));
            }

            return potions;
        }

        return null;
    }

    public static void writeNBTPotions(String id, PotionEffect[] potions, NBTTagCompound compound)
    {
        if (potions != null)
        {
            NBTTagList nbtTagList = new NBTTagList();

            for (PotionEffect p : potions)
            {
                nbtTagList.appendTag(p.writeCustomPotionEffectToNBT(new NBTTagCompound()));
            }

            compound.setTag(id, nbtTagList);
        }
    }

    public static int[] readIntArrayFixedSize(String id, int length, NBTTagCompound compound)
    {
        int[] array = compound.getIntArray(id);
        return array.length != length ? new int[length] : array;
    }
}
