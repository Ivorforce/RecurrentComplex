/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.genericStructures;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.maze.MazeRoom;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.*;

/**
 * Created by lukas on 01.02.15.
 */
public class Selection extends ArrayList<Selection.Area>
{
    public Collection<MazeRoom> mazeRooms(boolean additive)
    {
        Set<MazeRoom> mazeRooms = new HashSet<>();

        for (Area area : this)
            mergeRooms(additive == area.additive, 0, area.minCoord, area.maxCoord, area.minCoord.clone(), mazeRooms);

        return mazeRooms;
    }

    public void readFromNBT(NBTTagCompound compound, int dimensions)
    {
        clear();
        NBTTagList list = compound.getTagList("areas", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++)
            add(new Area(list.getCompoundTagAt(i), dimensions));
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        NBTTagList list = new NBTTagList();
        for (Area area : this)
            list.appendTag(area.writeToNBT());
        compound.setTag("areas", list);
    }

    public int[] boundsLower()
    {
        int[] min = null;

        for (Area area : this)
        {
            if (min == null)
                min = area.minCoord;
            else
            {
                for (int d = 0; d < area.minCoord.length; d++)
                    min[d] = Math.min(min[d], area.minCoord[d]);
            }
        }

        return min;
    }

    public int[] boundsHigher()
    {
        int[] max = null;

        for (Area area : this)
        {
            if (max == null)
                max = area.maxCoord;
            else
            {
                for (int d = 0; d < area.maxCoord.length; d++)
                    max[d] = Math.max(max[d], area.maxCoord[d]);
            }
        }

        return max;
    }

    private static void mergeRooms(boolean additive, int dimIndex, int[] min, int[] max, int[] position, Set<MazeRoom> rooms)
    {
        for (int i = min[dimIndex]; i <= max[dimIndex]; i++)
        {
            position[dimIndex] = i;

            if (dimIndex == position.length - 1)
            {
                if (additive)
                    rooms.add(new MazeRoom(position.clone()));
                else
                    rooms.remove(new MazeRoom(position));
            }
            else
                mergeRooms(additive, dimIndex + 1, min, max, position, rooms);
        }
    }

    public static class Area
    {
        private boolean additive;
        private int[] minCoord;
        private int[] maxCoord;

        public Area(boolean additive, IntegerRange... ranges)
        {
            this.additive = additive;
            minCoord = new int[ranges.length];
            maxCoord = new int[ranges.length];

            for (int i = 0; i < ranges.length; i++)
            {
                minCoord[i] = ranges[i].getMin();
                maxCoord[i] = ranges[i].getMax();
            }
        }

        public Area(boolean additive, int[] minCoord, int[] maxCoord)
        {
            this.additive = additive;
            this.minCoord = minCoord;
            this.maxCoord = maxCoord;
        }

        public Area(NBTTagCompound tagCompound, int dimensions)
        {
            additive = tagCompound.getBoolean("additive");
            minCoord = IvNBTHelper.readIntArrayFixedSize("min", dimensions, tagCompound);
            maxCoord = IvNBTHelper.readIntArrayFixedSize("max", dimensions, tagCompound);
        }

        public NBTTagCompound writeToNBT()
        {
            NBTTagCompound compound = new NBTTagCompound();

            compound.setBoolean("additive", additive);
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

        public boolean isAdditive()
        {
            return additive;
        }

        public void setAdditive(boolean additive)
        {
            this.additive = additive;
        }
    }
}
