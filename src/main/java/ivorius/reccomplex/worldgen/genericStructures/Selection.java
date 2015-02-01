/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.genericStructures;

import com.google.gson.annotations.SerializedName;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.maze.MazeRoom;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lukas on 01.02.15.
 */
public class Selection extends ArrayList<Selection.Area>
{
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

    public static Selection zeroSelection(int dimensions)
    {
        Selection selection = new Selection();
        selection.add(new Area(true, new int[dimensions], new int[dimensions]));
        return selection;
    }

    public Collection<MazeRoom> mazeRooms(boolean additive)
    {
        if (additive)
        {
            Set<MazeRoom> rooms = new HashSet<>();
            for (Area area : this)
                mergeRooms(area.additive, 0, area.minCoord, area.maxCoord, area.minCoord.clone(), rooms);
            return rooms;
        }
        else
        {
            Set<MazeRoom> spaces = new HashSet<>();
            int[] min = boundsLower();
            int[] max = boundsHigher();
            mergeRooms(true, 0, min, max, min.clone(), spaces);

            for (Area area : this)
                mergeRooms(!area.additive, 0, area.minCoord, area.maxCoord, area.minCoord.clone(), spaces);

            return spaces;
        }
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

    public static class Area
    {
        @SerializedName("additive")
        private boolean additive;
        @SerializedName("minCoord")
        private int[] minCoord;
        @SerializedName("maxCoord")
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
