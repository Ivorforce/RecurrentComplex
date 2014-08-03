/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import ivorius.reccomplex.blocks.TileEntityMazeGenerator;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditMazeBlock extends PacketFullTileEntityData
{
    public PacketEditMazeBlock()
    {
    }

    public PacketEditMazeBlock(TileEntityMazeGenerator mazeGenerator)
    {
        super(mazeGenerator.xCoord, mazeGenerator.yCoord, mazeGenerator.zCoord, getMazeNBT(mazeGenerator));
    }

    private static NBTTagCompound getMazeNBT(TileEntityMazeGenerator mazeGenerator)
    {
        NBTTagCompound compound = new NBTTagCompound();
        mazeGenerator.writeMazeDataToNBT(compound);
        return compound;
    }
}
