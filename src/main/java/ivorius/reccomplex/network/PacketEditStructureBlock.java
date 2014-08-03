/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import ivorius.reccomplex.blocks.TileEntityStructureGenerator;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditStructureBlock extends PacketFullTileEntityData
{
    public PacketEditStructureBlock()
    {
    }

    public PacketEditStructureBlock(TileEntityStructureGenerator structureGenerator)
    {
        super(structureGenerator.xCoord, structureGenerator.yCoord, structureGenerator.zCoord, getMazeNBT(structureGenerator));
    }

    private static NBTTagCompound getMazeNBT(TileEntityStructureGenerator structureGenerator)
    {
        NBTTagCompound compound = new NBTTagCompound();
        structureGenerator.writeStructureDataToNBT(compound);
        return compound;
    }
}
