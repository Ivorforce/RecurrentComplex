/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.util.WeightedRandom;

/**
 * Created by lukas on 20.01.15.
 */
public class WeightedObject<V> extends WeightedRandom.Item
{
    public V value;

    public WeightedObject(int weight, V value)
    {
        super(weight);
        this.value = value;
    }
}
