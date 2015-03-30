/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by lukas on 30.03.15.
 */
public class NBTNone implements NBTStorable
{
    @Override
    public NBTBase writeToNBT()
    {
        return new NBTTagCompound();
    }
}
