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

package ivorius.structuregen.ivtoolkit;

import net.minecraft.util.MathHelper;

import java.util.Random;

public class IvMathHelper
{
    public static double length(double[] vector)
    {
        double max = 0.0;

        for (int i = 0; i < vector.length; i++)
        {
            max += vector[i] * vector[i];
        }

        return MathHelper.sqrt_double(max);
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

    public static double mix(double value1, double value2, double progress)
    {
        return value1 + (value2 - value1) * progress;
    }

    public static double mixEaseInOut(double value1, double value2, double progress)
    {
        return cubicMix(value1, value1, value2, value2, progress);
    }

    public static double easeZeroToOne(double progress)
    {
        return cubicMix(0.0, 0.0, 1.0, 1.0, clamp(0.0, progress, 1.0));
    }

    public static double[] mix(double[] pos1, double[] pos2, double progress)
    {
        double[] result = new double[pos1.length];

        for (int i = 0; i < result.length; i++)
        {
            result[i] = mix(pos1[i], pos2[i], progress);
        }

        return result;
    }

    public static double quadraticMix(double value1, double value2, double value3, double progress)
    {
        return mix(mix(value1, value2, progress), mix(value2, value3, progress), progress);
    }

    public static double cubicMix(double value1, double value2, double value3, double value4, double progress)
    {
        return mix(quadraticMix(value1, value2, value3, progress), quadraticMix(value2, value3, value4, progress), progress);
    }

    public static double[] cubicMix(double[] pos1, double[] pos2, double[] pos3, double[] pos4, double progress)
    {
        double[] result = new double[pos1.length];

        for (int i = 0; i < result.length; i++)
        {
            result[i] = cubicMix(pos1[i], pos2[i], pos3[i], pos4[i], progress);
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

    public static double[] difference(double[] pos1, double[] pos2)
    {
        double[] result = new double[pos1.length];

        for (int i = 0; i < result.length; i++)
        {
            result[i] = pos1[i] - pos2[i];
        }

        return result;
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

    public static float clamp(float min, float value, float max)
    {
        if (value < min)
        {
            return min;
        }
        if (value > max)
        {
            return max;
        }

        return value;
    }

    public static double clamp(double min, double value, double max)
    {
        if (value < min)
        {
            return min;
        }
        if (value > max)
        {
            return max;
        }

        return value;
    }

    public static float nearValue(float value, float dest, float mulSpeed, float plusSpeed)
    {
        value += (dest - value) * mulSpeed;

        if (value > dest)
        {
            value -= plusSpeed;
            if (value < dest)
            {
                value = dest;
            }
        }
        else if (value < dest)
        {
            value += plusSpeed;
            if (value > dest)
            {
                value = dest;
            }
        }

        return value;
    }

    public static double nearValue(double value, double dest, double mulSpeed, double plusSpeed)
    {
        value += (dest - value) * mulSpeed;

        if (value > dest)
        {
            value -= plusSpeed;
            if (value < dest)
            {
                value = dest;
            }
        }
        else if (value < dest)
        {
            value += plusSpeed;
            if (value > dest)
            {
                value = dest;
            }
        }

        return value;
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

    public static int randomLinearNumber(Random random, float number)
    {
        return MathHelper.floor_float(number) + ((random.nextFloat() < (number % 1.0f)) ? 1 : 0);
    }

    public static float zeroToOne(float value, float min, float max)
    {
        return clamp(0.0f, (value - min) / (max - min), 1.0f);
    }
}
