/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.random;

import ivorius.ivtoolkit.math.IvVecMathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 12.10.14.
 */
public class BlurredValueField
{
    private final List<Value> values = new ArrayList<>();

    private int[] size;

    public BlurredValueField(int... size)
    {
        this.size = size;
    }

    public void addValue(Value value)
    {
        values.add(value);
    }

    public void addValue(float value, Random random)
    {
        int[] pos = new int[size.length];
        for (int i = 0; i < pos.length; i++)
            pos[i] = random.nextInt(size[i]);

        addValue(new Value(value, pos));
    }

    public float getValue(int... position)
    {
        double[] pos = new double[position.length];
        for (int i = 0; i < pos.length; i++)
            pos[i] = position[i];

        float total = 0;
        float[] inf = new float[values.size()];
        for (int i = 0; i < inf.length; i++)
        {
            Value value = values.get(i);

            double[] valPos = new double[size.length];
            for (int j = 0; j < valPos.length; j++)
                valPos[j] = value.pos[j];

            double dist = IvVecMathHelper.distanceSQ(pos, valPos);
            dist = dist * dist;
            dist = dist * dist;

            if (dist <= 0.0001)
                return value.value;

            inf[i] = (float) (1.0 / dist);
            total += inf[i];
        }

        float retVal = 0.0f;

        for (int i = 0; i < inf.length; i++)
             retVal += values.get(i).value * (inf[i] / total);

        return retVal;
    }

    public static class Value
    {
        private float value;
        private int[] pos;

        public Value(float value, int[] pos)
        {
            this.value = value;
            this.pos = pos;
        }
    }
}
