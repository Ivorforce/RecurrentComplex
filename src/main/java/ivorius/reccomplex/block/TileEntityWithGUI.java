/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.block;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by lukas on 11.02.15.
 */
public interface TileEntityWithGUI
{
    void writeSyncedNBT(NBTTagCompound compound);

    void readSyncedNBT(NBTTagCompound compound);

    @SideOnly(Side.CLIENT)
    void openEditGUI();
}
