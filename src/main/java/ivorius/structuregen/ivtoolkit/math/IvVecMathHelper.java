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

package ivorius.structuregen.ivtoolkit.math;

import ivorius.structuregen.ivtoolkit.math.IvMathHelper;
import net.minecraft.util.MathHelper;

/**
 * Created by lukas on 22.06.14.
 */
public class IvVecMathHelper
{
    public static double lengthSQ(double[] vector)
    {
        double max = 0.0;

        for (double aValue : vector)
        {
            max += aValue * aValue;
        }

        return max;
    }

    public static double length(double[] vector)
    {
        return MathHelper.sqrt_double(lengthSQ(vector));
    }

    public static double distanceSQ(double[] pos1, double[] pos2)
    {
        double distanceSQ = 0.0;

        for (int i = 0; i < pos1.length; i++)
        {
            distanceSQ += (pos1[i] - pos2[i]) * (pos1[i] - pos2[i]);
        }

        return distanceSQ;
    }

    public static double distance(double[] pos1, double[] pos2)
    {
        return MathHelper.sqrt_double(distanceSQ(pos1, pos2));
    }

    public static double[] mix(double[] pos1, double[] pos2, double progress)
    {
        double[] result = new double[pos1.length];

        for (int i = 0; i < result.length; i++)
        {
            result[i] = IvMathHelper.mix(pos1[i], pos2[i], progress);
        }

        return result;
    }

    public static double[] cubicMix(double[] pos1, double[] pos2, double[] pos3, double[] pos4, double progress)
    {
        double[] result = new double[pos1.length];

        for (int i = 0; i < result.length; i++)
        {
            result[i] = IvMathHelper.cubicMix(pos1[i], pos2[i], pos3[i], pos4[i], progress);
        }

        return result;
    }

    public static double[] normalize(double[] vector)
    {
        double max = length(vector);

        double[] resultVector = new double[vector.length];
        for (int i = 0; i < vector.length; i++)
        {
            resultVector[i] = vector[i] / max;
        }

        return resultVector;
    }

    public static double dotProduct(double[] pos1, double[] pos2)
    {
        double dotProduct = 0.0;

        for (int i = 0; i < pos1.length; i++)
        {
            dotProduct += pos1[i] * pos2[i];
        }

        return dotProduct;
    }

    public static double[] perpendicularVector(double[] pos1, double[] premiseVector)
    {
        double[] resultVector = new double[pos1.length];
        double dotProduct = 0.0;

        for (int i = 0; i < pos1.length - 1; i++)
        {
            dotProduct += pos1[i] * premiseVector[i];
            resultVector[i] = premiseVector[i];
        }

        resultVector[pos1.length - 1] = -(dotProduct / pos1[pos1.length - 1]);

        return normalize(resultVector);
    }

    public static double[] crossProduct(double[] pos1, double[] pos2)
    {
        double[] resultVector = new double[pos1.length];

        resultVector[0] = pos1[1] * pos2[2] - pos1[2] * pos2[1];
        resultVector[1] = pos1[2] * pos2[0] - pos1[0] * pos2[2];
        resultVector[2] = pos1[0] * pos2[1] - pos1[1] * pos2[0];

        return normalize(resultVector);
    }

    /**
     * Calculates cartesian from spherical coordinates.
     *
     * @param vector The spherical vector {azimuth, inclination, radius}
     * @return The cartesian vector {x, y, z}
     */
    public static double[] cartesianFromSpherical(double[] vector)
    {
        double sinInclination = MathHelper.sin((float) vector[1]);

        double x = vector[2] * sinInclination * MathHelper.cos((float) vector[0]);
        double y = vector[2] * MathHelper.cos((float) vector[1]);
        double z = vector[2] * sinInclination * MathHelper.sin((float) vector[0]);

        return new double[]{x, y, z};
    }

    /**
     * Calculates spherical from cartesian coordinates.
     *
     * @param vector The cartesian vector {x, y, z}
     * @return The spherical vector {azimuth, inclination, radius}
     */
    public static double[] sphericalFromCartesian(double[] vector)
    {
        double r = length(vector);
        double inclination = Math.acos(vector[1] / r);
        double azimuth = Math.atan2(vector[2], vector[0]);

        return new double[]{azimuth, inclination, r};
    }

    public static int[] add(int[]... vectors)
    {
        int[] result = new int[vectors[0].length];
        for (int i = 0; i < result.length; i++)
        {
            for (int[] vector : vectors)
            {
                result[i] += vector[i];
            }
        }

        return result;
    }

    public static double[] add(double[]... vectors)
    {
        double[] result = new double[vectors[0].length];
        for (int i = 0; i < result.length; i++)
        {
            for (double[] vector : vectors)
            {
                result[i] += vector[i];
            }
        }

        return result;
    }

    public static int[] sub(int[] vector, int[]... subVectors)
    {
        int[] result = vector.clone();
        for (int i = 0; i < result.length; i++)
        {
            for (int[] subVector : subVectors)
            {
                result[i] -= subVector[i];
            }
        }

        return result;
    }

    public static double[] sub(double[] vector, double[]... subVectors)
    {
        double[] result = vector.clone();
        for (int i = 0; i < result.length; i++)
        {
            for (double[] subVector : subVectors)
            {
                result[i] -= subVector[i];
            }
        }

        return result;
    }
}
