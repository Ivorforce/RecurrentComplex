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

package ivorius.ivtoolkit.maze;

import ivorius.ivtoolkit.math.IvVecMathHelper;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;

/**
 * Created by lukas on 23.06.14.
 */
public class MazeRoom implements MazeCoordinate, Cloneable
{
    public final int[] coordinates;

    public MazeRoom(int... coordinates)
    {
        this.coordinates = coordinates;
    }

    public MazeRoom(NBTTagCompound compound)
    {
        coordinates = compound.getIntArray("coordinates");
    }

    public int getDimensions()
    {
        return coordinates.length;
    }

    public int[] getCoordinates()
    {
        return coordinates;
    }

    public MazeRoom add(MazeRoom room)
    {
        return new MazeRoom(IvVecMathHelper.add(coordinates, room.coordinates));
    }

    public MazeRoom sub(MazeRoom room)
    {
        return new MazeRoom(IvVecMathHelper.sub(coordinates, room.coordinates));
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

        MazeRoom mazeRoom = (MazeRoom) o;

        if (!Arrays.equals(coordinates, mazeRoom.coordinates))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(coordinates);
    }

    @Override
    public String toString()
    {
        return Arrays.toString(coordinates);
    }

    @Override
    public MazeRoom clone()
    {
        return new MazeRoom(coordinates.clone());
    }

    @Override
    public int[] getMazeCoordinates()
    {
        int[] coords = new int[coordinates.length];
        for (int i = 0; i < coords.length; i++)
        {
            coords[i] = coordinates[i] * 2 + 1;
        }

        return coords;
    }

    public NBTTagCompound writeToNBT()
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setIntArray("coordinates", coordinates);
        return compound;
    }
}
