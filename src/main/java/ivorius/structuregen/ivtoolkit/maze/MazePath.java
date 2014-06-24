/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.structuregen.ivtoolkit.maze;

import ivorius.structuregen.ivtoolkit.math.IvVecMathHelper;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by lukas on 23.06.14.
 */
public class MazePath implements MazeCoordinate, Cloneable
{
    public final MazeRoom sourceRoom;
    public int pathDimension;
    public boolean pathGoesUp;

    public MazePath(MazeRoom sourceRoom, int pathDimension, boolean pathGoesUp)
    {
        this.sourceRoom = sourceRoom;
        this.pathDimension = pathDimension;
        this.pathGoesUp = pathGoesUp;
    }

    public MazePath(int pathDimension, boolean pathGoesUp, int... roomCoordinates)
    {
        this(new MazeRoom(roomCoordinates), pathDimension, pathGoesUp);
    }

    public MazePath(NBTTagCompound compound)
    {
        sourceRoom = new MazeRoom(compound.getCompoundTag("source"));
        pathDimension = compound.getInteger("pathDimension");
        pathGoesUp = compound.getBoolean("pathGoesUp");
    }

    public static MazePath pathFromSourceAndDest(MazeRoom source, MazeRoom dest)
    {
        for (int i = 0; i < source.coordinates.length; i++)
        {
            if (source.coordinates[i] != dest.coordinates[i])
                return new MazePath(source, i, source.coordinates[i] < dest.coordinates[i]);
        }

        return null;
    }

    public MazePath invertPath()
    {
        return new MazePath(getDestinationRoom(), pathDimension, !pathGoesUp);
    }

    public int[] getPathDirection()
    {
        int[] direction = new int[sourceRoom.coordinates.length];
        direction[pathDimension] = pathGoesUp ? 1 : -1;
        return direction;
    }

    public MazeRoom getSourceRoom()
    {
        return sourceRoom;
    }

    public MazeRoom getDestinationRoom()
    {
        return new MazeRoom(IvVecMathHelper.add(sourceRoom.getCoordinates(), getPathDirection()));
    }

    public MazePath add(MazeRoom room)
    {
        return new MazePath(sourceRoom.add(room), pathDimension, pathGoesUp);
    }

    public MazePath sub(MazeRoom room)
    {
        return new MazePath(sourceRoom.sub(room), pathDimension, pathGoesUp);
    }

    @Override
    protected MazePath clone()
    {
        return new MazePath(sourceRoom.clone(), pathDimension, pathGoesUp);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        MazePath mazePath = (MazePath) o;

        return pathDimension == mazePath.pathDimension
                && pathGoesUp == mazePath.pathGoesUp ? sourceRoom.equals(mazePath.sourceRoom) : invertPath().sourceRoom.equals(mazePath.sourceRoom);
    }

    @Override
    public int hashCode()
    {
        int result = sourceRoom.hashCode();
        result = 31 * result + pathDimension;
        result = 31 * result + (pathGoesUp ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return getSourceRoom().toString() + " -> " + getDestinationRoom();
    }

    @Override
    public int[] getMazeCoordinates()
    {
        int[] coords = sourceRoom.getMazeCoordinates();
        coords[pathDimension] += pathGoesUp ? 1 : -1;
        return coords;
    }

    public NBTTagCompound writeToNBT()
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("source", sourceRoom.writeToNBT());
        compound.setInteger("pathDimension", pathDimension);
        compound.setBoolean("pathGoesUp", pathGoesUp);
        return compound;
    }
}
