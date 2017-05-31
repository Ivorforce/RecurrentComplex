/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.nbt;

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
