/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.io.DataOutput;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by lukas on 18.01.15.
 */
public class RCAccessorNBT
{
    private static Method methodSetNBTTagName;

    public static void writeEntry(String name, NBTBase nbt, DataOutput dataOutput)
    {
        if (methodSetNBTTagName == null)
            methodSetNBTTagName = ReflectionHelper.findMethod(NBTTagCompound.class, new NBTTagCompound(),
                    new String[]{"func_150298_a", "writeEntry"},
                    String.class, NBTBase.class, DataOutput.class);

        try
        {
            methodSetNBTTagName.invoke(null, name, nbt, dataOutput);
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }
}
