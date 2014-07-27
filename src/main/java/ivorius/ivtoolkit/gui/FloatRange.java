/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.ivtoolkit.gui;

/**
 * Created by lukas on 13.06.14.
 */
public class FloatRange
{
    public final float min;
    public final float max;

    public FloatRange(float min, float max)
    {
        this.min = min;
        this.max = max;
    }

    public FloatRange(IntegerRange integerRange)
    {
        min = integerRange.getMin();
        max = integerRange.getMax();
    }

    public float getMin()
    {
        return min;
    }

    public float getMax()
    {
        return max;
    }

    @Override
    public String toString()
    {
        return "FloatRange{" +
                "min=" + min +
                ", max=" + max +
                '}';
    }
}
