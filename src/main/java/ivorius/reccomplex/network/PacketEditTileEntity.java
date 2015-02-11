/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import ivorius.reccomplex.blocks.TileEntityWithGUI;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditTileEntity extends PacketFullTileEntityData
{
    public PacketEditTileEntity()
    {
    }

    public <TE extends TileEntity & TileEntityWithGUI> PacketEditTileEntity(TE tileEntity)
    {
        super(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, getSyncedNBT(tileEntity));
    }

    private static NBTTagCompound getSyncedNBT(TileEntityWithGUI structureGenerator)
    {
        NBTTagCompound compound = new NBTTagCompound();
        structureGenerator.writeSyncedNBT(compound);
        return compound;
    }
}
