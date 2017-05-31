/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.presets;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ivorius.reccomplex.files.loading.RCFileSuffix;
import ivorius.reccomplex.world.gen.feature.structure.generic.WeightedDimensionMatcher;
import ivorius.reccomplex.utils.presets.PresetRegistry;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by lukas on 26.02.15.
 */
public class DimensionMatcherPresets extends PresetRegistry<ArrayList<WeightedDimensionMatcher>>
{

    private static DimensionMatcherPresets instance;

    public DimensionMatcherPresets(String fileSuffix)
    {
        super(fileSuffix, "dimension preset");
    }

    public static DimensionMatcherPresets instance()
    {
        return instance != null ? instance : (instance = new DimensionMatcherPresets(RCFileSuffix.DIMENSION_PRESET));
    }

    @Override
    protected void registerGson(GsonBuilder builder)
    {
        builder.registerTypeAdapter(WeightedDimensionMatcher.class, new WeightedDimensionMatcher.Serializer());
    }

    @Override
    protected Type getType()
    {
        return new TypeToken<ArrayList<WeightedDimensionMatcher>>(){}.getType();
    }
}
