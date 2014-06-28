/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.ivtoolkit.math;

import net.minecraft.util.MathHelper;

import java.util.Random;

public class IvMathHelper
{

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

    public static double quadraticMix(double value1, double value2, double value3, double progress)
    {
        return mix(mix(value1, value2, progress), mix(value2, value3, progress), progress);
    }

    public static double cubicMix(double value1, double value2, double value3, double value4, double progress)
    {
        return mix(quadraticMix(value1, value2, value3, progress), quadraticMix(value2, value3, value4, progress), progress);
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

    public static int randomLinearNumber(Random random, float number)
    {
        return MathHelper.floor_float(number) + ((random.nextFloat() < (number % 1.0f)) ? 1 : 0);
    }

    public static float zeroToOne(float value, float min, float max)
    {
        return clamp(0.0f, (value - min) / (max - min), 1.0f);
    }
}
