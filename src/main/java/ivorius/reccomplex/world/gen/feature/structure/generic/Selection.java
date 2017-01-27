/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic;

import com.google.gson.annotations.SerializedName;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.ivtoolkit.tools.NBTTagLists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public Selection(int dimensions, Collection<? extends Area> c)
    {
        super(c);
        this.dimensions = dimensions;
    }

    private static void mergeRooms(boolean additive, int dimIndex, int[] min, int[] max, int[] position, String id, Map<MazeRoom, String> rooms)
    {
        for (int i = min[dimIndex]; i <= max[dimIndex]; i++)
        {
            position[dimIndex] = i;

            if (dimIndex == position.length - 1)
            {
                if (additive)
                    rooms.put(new MazeRoom(position), id);
                else
                    rooms.remove(new MazeRoom(position));
            }
            else
                mergeRooms(additive, dimIndex + 1, min, max, position, id, rooms);
        }
    }

    public static Selection zeroSelection(int dimensions)
    {
        Selection selection = new Selection(dimensions);
        selection.add(new Area(true, new int[dimensions], new int[dimensions]));
        return selection;
    }

    @Nullable
    public static List<IntegerRange> toRanges(int[] lower, int[] higher)
    {
        return lower != null
                ? IntStream.range(0, lower.length).mapToObj(i -> new IntegerRange(lower[i], higher[i])).collect(Collectors.toList())
                : null;
    }

    public Map<MazeRoom, String> compile(boolean additive)
    {
        if (additive)
        {
            Map<MazeRoom, String> rooms = new HashMap<>();
            for (Area area : this)
                mergeRooms(area.additive, 0, area.minCoord, area.maxCoord, area.minCoord.clone(), area.identifier, rooms);
            return rooms;
        }
        else
        {
            Map<MazeRoom, String> spaces = new HashMap<>();
            int[] min = boundsLower();
            int[] max = boundsHigher();
            mergeRooms(true, 0, min, max, min.clone(), null, spaces);

            for (Area area : this)
                mergeRooms(!area.additive, 0, area.minCoord, area.maxCoord, area.minCoord.clone(), area.identifier, spaces);

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

    @Nullable
    public List<IntegerRange> bounds()
    {
        return toRanges(boundsLower(), boundsHigher());
    }

    @Nullable
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

    @Nullable
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
        for (int i = 0; i < max.length; i++) max[i] += 1 - min[i];
        return max;
    }

    public Selection copy()
    {
        Selection selection = new Selection(dimensions);
        selection.addAll(stream().map(Area::copy).collect(Collectors.toList()));
        return selection;
    }

    public void transform(AxisAlignedTransform2D transform, int[] size)
    {
        forEach((area) -> area.transform(transform, size));
    }

    public static class Area
    {
        @SerializedName("additive")
        private boolean additive;
        @SerializedName("minCoord")
        private int[] minCoord;
        @SerializedName("maxCoord")
        private int[] maxCoord;
        @Nullable
        @SerializedName("identifier")
        private String identifier;

        public Area(boolean additive, int[] minCoord, int[] maxCoord)
        {
            this.additive = additive;
            this.minCoord = minCoord;
            this.maxCoord = maxCoord;

            if (minCoord.length != maxCoord.length)
                throw new IllegalArgumentException();
        }

        public Area(boolean additive, int[] minCoord, int[] maxCoord, @Nullable String identifier)
        {
            this.additive = additive;
            this.minCoord = minCoord;
            this.maxCoord = maxCoord;
            this.identifier = identifier;

            if (minCoord.length != maxCoord.length)
                throw new IllegalArgumentException();
        }

        public Area(NBTTagCompound tagCompound, int dimensions)
        {
            additive = tagCompound.getBoolean("additive");
            minCoord = IvNBTHelper.readIntArrayFixedSize("min", dimensions, tagCompound);
            maxCoord = IvNBTHelper.readIntArrayFixedSize("max", dimensions, tagCompound);
            identifier = tagCompound.hasKey("identifier", Constants.NBT.TAG_STRING)
                    ? tagCompound.getString("identifier") : null;
        }

        public NBTTagCompound writeToNBT()
        {
            NBTTagCompound compound = new NBTTagCompound();

            compound.setBoolean("additive", additive);
            compound.setIntArray("min", minCoord);
            compound.setIntArray("max", maxCoord);
            if (identifier != null) compound.setString("identifier", identifier);

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

        @Nullable
        public String getIdentifier()
        {
            return identifier;
        }

        public void setIdentifier(@Nullable String identifier)
        {
            this.identifier = identifier;
        }

        public Area copy()
        {
            return new Area(additive, minCoord.clone(), maxCoord.clone(), identifier);
        }

        public void transform(AxisAlignedTransform2D transform, int[] size)
        {
            transform.applyOn(minCoord, size, 1);
            transform.applyOn(maxCoord, size, 1);

            for (int i = 0; i < minCoord.length; i++)
            {
                int min = Math.min(minCoord[i], maxCoord[i]);
                int max = Math.max(minCoord[i], maxCoord[i]);

                this.minCoord[i] = min;
                this.maxCoord[i] = max;
            }
        }

        public boolean includes(Area area)
        {
            for (int i = 0; i < minCoord.length; i++)
            {
                if (minCoord[i] > area.minCoord[i] || maxCoord[i] < area.maxCoord[i])
                    return false;
            }

            return true;
        }

        @Nullable
        public Area intersection(Area area)
        {
            if (area.minCoord.length != minCoord.length)
                throw new IllegalArgumentException();

            int[] min = new int[minCoord.length];
            int[] max = new int[minCoord.length];

            for (int i = 0; i < min.length; i++)
            {
                min[i] = Math.max(min[i], area.minCoord[i]);
                max[i] = Math.min(max[i], area.maxCoord[i]);

                if (min[i] > max[i])
                    return null;
            }

            return new Area(additive, min, max);
        }
    }
}
