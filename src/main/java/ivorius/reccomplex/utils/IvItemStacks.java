/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.Constants;

/**
 * Created by lukas on 17.01.15.
 */
public class IvItemStacks
{
    public static int getNBTInt(ItemStack stack, String key, int defaultValue)
    {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey(key, Constants.NBT.TAG_INT)
                ? stack.getTagCompound().getInteger(key)
                : defaultValue;
    }

    public static float getNBTFloat(ItemStack stack, String key, float defaultValue)
    {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey(key, Constants.NBT.TAG_FLOAT)
                ? stack.getTagCompound().getFloat(key)
                : defaultValue;
    }
}
