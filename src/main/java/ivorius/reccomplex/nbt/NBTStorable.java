/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.nbt;

import net.minecraft.nbt.NBTBase;

/**
 * Created by lukas on 30.03.15.
 */
public interface NBTStorable
{
    NBTBase writeToNBT();
}
