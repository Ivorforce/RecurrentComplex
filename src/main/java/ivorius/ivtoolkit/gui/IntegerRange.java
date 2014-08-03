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

package ivorius.ivtoolkit.gui;

import net.minecraft.util.MathHelper;

/**
 * Created by lukas on 13.06.14.
 */
public class IntegerRange
{
    public final int min;
    public final int max;

    public IntegerRange(int min, int max)
    {
        this.min = min;
        this.max = max;
    }

    public IntegerRange(FloatRange floatRange)
    {
        min = MathHelper.floor_float(floatRange.getMin());
        max = MathHelper.floor_float(floatRange.getMax());
    }

    public int getMin()
    {
        return min;
    }

    public int getMax()
    {
        return max;
    }

    @Override
    public String toString()
    {
        return "IntegerRange{" +
                "min=" + min +
                ", max=" + max +
                '}';
    }
}
