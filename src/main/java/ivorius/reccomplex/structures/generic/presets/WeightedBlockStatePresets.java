/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.presets;

import com.google.gson.Gson;
import ivorius.reccomplex.structures.generic.WeightedBlockState;
import ivorius.reccomplex.utils.ListPresets;

/**
 * Created by lukas on 03.03.15.
 */
public class WeightedBlockStatePresets extends ListPresets<WeightedBlockState>
{
    private static WeightedBlockStatePresets instance;

    public static WeightedBlockStatePresets instance()
    {
        return instance != null ? instance : (instance = new WeightedBlockStatePresets());
    }


    @Override
    protected Gson createGson()
    {
        return WeightedBlockState.getGson();
    }

    @Override
    protected Class<WeightedBlockState[]> getType()
    {
        return WeightedBlockState[].class;
    }
}
