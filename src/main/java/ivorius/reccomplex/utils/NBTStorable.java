/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.nbt.NBTBase;

/**
 * Created by lukas on 30.03.15.
 */
public interface NBTStorable
{
    NBTBase writeToNBT();
}
