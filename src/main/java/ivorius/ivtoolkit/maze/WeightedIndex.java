/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.ivtoolkit.maze;

import net.minecraft.util.WeightedRandom;

/**
 * Created by lukas on 24.06.14.
 */
public class WeightedIndex extends WeightedRandom.Item
{
    private int index;

    public WeightedIndex(int weight, int index)
    {
        super(weight);
        this.index = index;
    }

    public int getIndex()
    {
        return index;
    }
}
