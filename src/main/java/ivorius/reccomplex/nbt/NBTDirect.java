/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.nbt;

import net.minecraft.nbt.NBTBase;

/**
 * Created by lukas on 30.03.15.
 */
public class NBTDirect implements NBTStorable
{
    public NBTBase nbt;

    public NBTDirect(NBTBase nbt)
    {
        this.nbt = nbt;
    }

    @Override
    public NBTBase writeToNBT()
    {
        return nbt;
    }
}
