/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.genericStructures;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.maze.MazeRoom;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by lukas on 08.10.14.
 */
public class MazeRoomArea
{
    private int[] minCoord;
    private int[] maxCoord;

    public MazeRoomArea(IntegerRange... ranges)
    {
        minCoord = new int[ranges.length];
        maxCoord = new int[ranges.length];

        for (int i = 0; i < ranges.length; i++)
        {
            minCoord[i] = ranges[i].getMin();
            maxCoord[i] = ranges[i].getMax();
        }
    }

    public MazeRoomArea(int[] minCoord, int[] maxCoord)
    {
        this.minCoord = minCoord;
        this.maxCoord = maxCoord;
    }

    public MazeRoomArea(NBTTagCompound tagCompound)
    {
        minCoord = tagCompound.getIntArray("min");
        maxCoord = tagCompound.getIntArray("max");
    }

    public NBTTagCompound writeToNBT()
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setIntArray("min", minCoord);
        compound.setIntArray("max", maxCoord);

        return compound;
    }

    public int[] getMinCoord()
    {
        return minCoord.clone();
    }

    public void setMinCoord(int[] minCoord)
    {
        this.minCoord = minCoord.clone();
    }

    public int[] getMaxCoord()
    {
        return maxCoord.clone();
    }

    public void setMaxCoord(int[] maxCoord)
    {
        this.maxCoord = maxCoord.clone();
    }

    public void setCoord(int dim, int min, int max)
    {
        minCoord[dim] = min;
        maxCoord[dim] = max;
    }

    public Collection<MazeRoom> mazeRooms()
    {
        List<MazeRoom> mazeRooms = new ArrayList<>();
        addRooms(0, minCoord, maxCoord, new int[minCoord.length], mazeRooms);
        return mazeRooms;
    }

    private static void addRooms(int dimIndex, int[] min, int[] max, int[] position, List<MazeRoom> rooms)
    {
        for (int i = min[dimIndex]; i <= max[dimIndex]; i++)
        {
            position[dimIndex] = i;

            if (dimIndex == position.length - 1)
                rooms.add(new MazeRoom(position.clone()));
            else
                addRooms(dimIndex + 1, min, max, position, rooms);
        }
    }
}
