/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.presets;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import ivorius.reccomplex.files.loading.RCFileSuffix;
import ivorius.reccomplex.world.gen.feature.structure.generic.WeightedBiomeMatcher;
import ivorius.reccomplex.utils.presets.PresetRegistry;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by lukas on 26.02.15.
 */
public class BiomeMatcherPresets extends PresetRegistry<ArrayList<WeightedBiomeMatcher>>
{

    private static BiomeMatcherPresets instance;

    public BiomeMatcherPresets(String fileSuffix)
    {
        super(fileSuffix, "biome preset");
    }

    public static BiomeMatcherPresets instance()
    {
        return instance != null ? instance : (instance = new BiomeMatcherPresets(RCFileSuffix.BIOME_PRESET));
    }

    @Override
    protected void registerGson(GsonBuilder builder)
    {
        builder.registerTypeAdapter(WeightedBiomeMatcher.class, new WeightedBiomeMatcher.Serializer());
    }

    @Override
    protected Type getType()
    {
        return new TypeToken<ArrayList<WeightedBiomeMatcher>>(){}.getType();
    }
}
