/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic;

import com.google.gson.annotations.SerializedName;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.ivtoolkit.tools.NBTTagLists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 01.02.15.
 */
public class Selection extends ArrayList<Selection.Area> implements NBTCompoundObject
{
    public final int dimensions;

    public Selection(int dimensions)
    {
        this.dimensions = dimensions;
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

    public static Selection zeroSelection(int dimensions)
    {
        Selection selection = new Selection(dimensions);
        selection.add(new Area(true, new int[dimensions], new int[dimensions]));
        return selection;
    }

    public Set<MazeRoom> mazeRooms(boolean additive)
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

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        clear();
        NBTTagLists.compoundsFrom(compound, "areas").forEach(cmp -> add(new Area(cmp, dimensions)));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        NBTTagLists.writeTo(compound, "areas", stream().map(Area::writeToNBT).collect(Collectors.toList()));
    }

    public int[] boundsLower()
    {
        int[] min = null;

        for (Area area : this)
        {
            if (min == null)
                min = area.minCoord.clone();
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
                max = area.maxCoord.clone();
            else
            {
                for (int d = 0; d < area.maxCoord.length; d++)
                    max[d] = Math.max(max[d], area.maxCoord[d]);
            }
        }

        return max;
    }

    public int[] boundsSize()
    {
        int[] min = boundsLower();
        int[] max = boundsHigher();
        int[] minusOne = new int[min.length];
        Arrays.fill(minusOne, -1);
        return IvVecMathHelper.sub(max, min, minusOne);
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
