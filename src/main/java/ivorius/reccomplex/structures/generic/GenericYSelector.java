/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic;

import com.google.gson.annotations.SerializedName;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.reccomplex.structures.YSelector;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class GenericYSelector implements YSelector
{
    public static final int MIN_DIST_TO_VOID = 3;

    public enum SelectionMode
    {
        @SerializedName("bedrock")
        BEDROCK,
        @SerializedName("surface")
        SURFACE,
        @SerializedName("sealevel")
        SEALEVEL,
        @SerializedName("underwater")
        UNDERWATER,
        @SerializedName("top")
        TOP,
        @SerializedName("lowestedge")
        LOWEST_EDGE;

        public String serializedName()
        {
            return IvGsonHelper.serializedName(this);
        }

        public static SelectionMode selectionMode(String serializedName)
        {
            return IvGsonHelper.enumForName(serializedName, values());
        }
    }

    public SelectionMode selectionMode;

    public int minY;
    public int maxY;

    public GenericYSelector(SelectionMode selectionMode, int minY, int maxY)
    {
        this.selectionMode = selectionMode;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public int generationY(final World world, Random random, StructureBoundingBox boundingBox)
    {
        final int y = minY + random.nextInt(maxY - minY + 1);

        switch (selectionMode)
        {
            case BEDROCK:
                return selectByConstant(world, y);
            case TOP:
                return selectByConstant(world, world.getHeight() + y);
            case SEALEVEL:
                return selectByConstant(world, 63 + y);
            case SURFACE:
                return selectByFunction(world, boundingBox, surfaceSelector(world), averageReducer(y));
            case UNDERWATER:
                return selectByFunction(world, boundingBox, surfaceUnderwaterSelector(world), averageReducer(y));
            case LOWEST_EDGE:
                return selectByFunction(world, boundingBox, surfaceUnderwaterSelector(world), minReducer(y));
        }

        throw new RuntimeException("Unrecognized selection mode " + selectionMode);
    }

    protected static SingleYSelector surfaceSelector(final World world)
    {
        return new SingleYSelector()
        {
            @Override
            public int select(int x, int z)
            {
                return surfaceHeight(world, x, z);
            }
        };
    }

    protected static SingleYSelector surfaceUnderwaterSelector(final World world)
    {
        return new SingleYSelector()
        {
            @Override
            public int select(int x, int z)
            {
                return surfaceHeightUnderwater(world, x, z);
            }
        };
    }

    protected static IntListReducer minReducer(final int y)
    {
        return new IntListReducer()
        {
            @Override
            public int reduce(TIntList list)
            {
                int average = list.min();
                return average > MIN_DIST_TO_VOID ? average + y : DONT_GENERATE;
            }
        };
    }

    protected static IntListReducer averageReducer(final int y)
    {
        return new IntListReducer()
        {
            @Override
            public int reduce(TIntList list)
            {
                int average = averageIgnoringErrors(list.toArray());
                return average > MIN_DIST_TO_VOID ? average + y : DONT_GENERATE;
            }
        };
    }

    protected static int surfaceHeightUnderwater(World world, int x, int z)
    {
        int curYWater = world.getTopSolidOrLiquidBlock(x, z);

        while (curYWater > 0)
        {
            Block block = world.getBlock(x, curYWater, z);
            if (!(block instanceof BlockLiquid || block.getMaterial() == Material.ice))
            {
                curYWater++;
                break;
            }

            curYWater--;
        }
        return curYWater;
    }

    protected static int surfaceHeight(World world, int x, int z)
    {
        int curY = world.getTopSolidOrLiquidBlock(x, z);

        while (curY > 0)
        {
            Block block = world.getBlock(x, curY, z);
            if (!(block.isFoliage(world, x, curY, z) || block.getMaterial() == Material.leaves || block.getMaterial() == Material.plants || block.getMaterial() == Material.wood))
            {
                break;
            }

            curY--;
        }
        while (curY < world.getHeight())
        {
            if (!(world.getBlock(x, curY, z) instanceof BlockLiquid))
            {
                break;
            }

            curY++;
        }

        return curY;
    }

    protected static int selectByConstant(World world, int y)
    {
        return MathHelper.clamp_int(y, MIN_DIST_TO_VOID, world.getHeight() - MIN_DIST_TO_VOID);
    }

    protected static int selectByFunction(World world, StructureBoundingBox boundingBox, SingleYSelector selector, IntListReducer reducer)
    {
        TIntList intList = selectAll(world, boundingBox, selector);

        if (intList.size() == 0)
            return DONT_GENERATE;

        return reducer.reduce(intList);
    }

    protected static TIntList selectAll(World world, StructureBoundingBox boundingBox, SingleYSelector selector)
    {
        TIntList list = new TIntArrayList();
        for (int x = boundingBox.minX; x <= boundingBox.maxX; x++)
            for (int z = boundingBox.minZ; z <= boundingBox.maxZ; z++)
            {
                if (world.blockExists(x, 0, z))
                    list.add(selector.select(x, z));
            }
        return list;
    }

    protected static int averageIgnoringErrors(int... values)
    {
        int average = 0;
        for (int val : values)
            average += val;
        average /= values.length;

        int averageDist = 0;
        for (int val : values)
            averageDist += dist(val, average);
        averageDist /= values.length;

        int newAverage = 0;
        int ignored = 0;
        for (int val : values)
        {
            if (dist(val, average) <= averageDist * 2)
                newAverage += val;
            else
                ignored++;
        }

        return newAverage / (values.length - ignored);
    }

    protected static int dist(int val1, int val2)
    {
        return (val1 > val2) ? val1 - val2 : val2 - val1;
    }

    protected interface IntListReducer
    {
        int reduce(TIntList list);
    }

    protected interface SingleYSelector
    {
        int select(int x, int z);
    }
}
