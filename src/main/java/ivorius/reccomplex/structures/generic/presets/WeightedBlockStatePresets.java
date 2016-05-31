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
    public static final String FILE_SUFFIX = "rcbm";

    private static WeightedBlockStatePresets instance;

    public WeightedBlockStatePresets(String fileSuffix)
    {
        super(fileSuffix);
    }

    public static WeightedBlockStatePresets instance()
    {
        return instance != null ? instance : (instance = new WeightedBlockStatePresets(FILE_SUFFIX));
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
