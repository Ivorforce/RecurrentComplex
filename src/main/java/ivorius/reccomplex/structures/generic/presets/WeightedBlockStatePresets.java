/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.presets;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.structures.generic.WeightedBlockState;
import ivorius.reccomplex.utils.PresetRegistry;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by lukas on 03.03.15.
 */
public class WeightedBlockStatePresets extends PresetRegistry<ArrayList<WeightedBlockState>>
{
    public static final String FILE_SUFFIX = "rcbm";

    private static WeightedBlockStatePresets instance;

    public WeightedBlockStatePresets(String fileSuffix)
    {
        super(fileSuffix, "block preset");
    }

    public static WeightedBlockStatePresets instance()
    {
        return instance != null ? instance : (instance = new WeightedBlockStatePresets(FILE_SUFFIX));
    }

    @Override
    protected void registerGson(GsonBuilder builder)
    {
        builder.registerTypeAdapter(WeightedBlockState.class, new WeightedBlockState.Serializer(RecurrentComplex.specialRegistry));
    }

    @Override
    protected Type getType()
    {
        return new TypeToken<ArrayList<WeightedBlockState>>(){}.getType();
    }
}
