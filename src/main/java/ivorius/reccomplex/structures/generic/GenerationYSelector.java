/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic;

import com.google.gson.annotations.SerializedName;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class GenerationYSelector
{
    public static final int DONT_GENERATE = -1;
    public static final int MIN_DIST_TO_VOID = 3;

    public static enum SelectionMode
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

    public GenerationYSelector(SelectionMode selectionMode, int minY, int maxY)
    {
        this.selectionMode = selectionMode;
        this.minY = minY;
        this.maxY = maxY;
    }

    public int generationY(World world, Random random, StructureBoundingBox boundingBox)
    {
        int y = minY + random.nextInt(maxY - minY + 1);

        switch (selectionMode)
        {
            case BEDROCK:
                return Math.max(2, y);
            case SURFACE:
            {
                int genYC = surfaceHeight(world, boundingBox.getCenterX(), boundingBox.getCenterZ());
                int genYPP = surfaceHeight(world, boundingBox.maxX, boundingBox.maxZ);
                int genYPM = surfaceHeight(world, boundingBox.maxX, boundingBox.minZ);
                int genYMP = surfaceHeight(world, boundingBox.minX, boundingBox.maxZ);
                int genYMM = surfaceHeight(world, boundingBox.minX, boundingBox.minZ);

                int avg = averageIgnoringErrors(genYC, genYPP, genYPM, genYMP, genYMM);
                return avg > MIN_DIST_TO_VOID ? avg + y : DONT_GENERATE;
            }
            case SEALEVEL:
                return 63 + y;
            case UNDERWATER:
            {
                int genYC = surfaceHeightUnderwater(world, boundingBox.getCenterX(), boundingBox.getCenterZ());
                int genYPP = surfaceHeightUnderwater(world, boundingBox.maxX, boundingBox.maxZ);
                int genYPM = surfaceHeightUnderwater(world, boundingBox.maxX, boundingBox.minZ);
                int genYMP = surfaceHeightUnderwater(world, boundingBox.minX, boundingBox.maxZ);
                int genYMM = surfaceHeightUnderwater(world, boundingBox.minX, boundingBox.minZ);

                int avg = averageIgnoringErrors(genYC, genYPP, genYPM, genYMP, genYMM);
                return avg > MIN_DIST_TO_VOID ? avg + y : DONT_GENERATE;
            }
            case TOP:
                return world.getHeight() + y;
            case LOWEST_EDGE:
            {
                int genYC = surfaceHeightUnderwater(world, boundingBox.getCenterX(), boundingBox.getCenterZ());
                int genYPP = surfaceHeightUnderwater(world, boundingBox.maxX, boundingBox.maxZ);
                int genYPM = surfaceHeightUnderwater(world, boundingBox.maxX, boundingBox.minZ);
                int genYMP = surfaceHeightUnderwater(world, boundingBox.minX, boundingBox.maxZ);
                int genYMM = surfaceHeightUnderwater(world, boundingBox.minX, boundingBox.minZ);

                int min = min(genYC, genYPP, genYPM, genYMP, genYMM);
                return min > MIN_DIST_TO_VOID ? min + y : DONT_GENERATE;
            }
        }

        throw new RuntimeException("Unrecognized selection mode " + selectionMode);
    }

    private int surfaceHeightUnderwater(World world, int x, int z)
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

    private int surfaceHeight(World world, int x, int z)
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

    private static int min(int... values)
    {
        int min = values[0];
        for (int val : values)
            min = Math.min(val, min);
        return min;
    }


    private int averageIgnoringErrors(int... values)
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

    private int dist(int val1, int val2)
    {
        return (val1 > val2) ? val1 - val2 : val2 - val1;
    }
}
