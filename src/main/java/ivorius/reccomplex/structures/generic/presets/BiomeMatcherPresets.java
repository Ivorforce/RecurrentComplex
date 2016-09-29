/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.presets;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import ivorius.reccomplex.structures.generic.BiomeGenerationInfo;
import ivorius.reccomplex.utils.PresetRegistry;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by lukas on 26.02.15.
 */
public class BiomeMatcherPresets extends PresetRegistry<ArrayList<BiomeGenerationInfo>>
{
    public static final String FILE_SUFFIX = "rcbp";

    private static BiomeMatcherPresets instance;

    public BiomeMatcherPresets(String fileSuffix)
    {
        super(fileSuffix, "biome preset");
    }

    public static BiomeMatcherPresets instance()
    {
        return instance != null ? instance : (instance = new BiomeMatcherPresets(FILE_SUFFIX));
    }

    @Override
    protected void registerGson(GsonBuilder builder)
    {
        builder.registerTypeAdapter(BiomeGenerationInfo.class, new BiomeGenerationInfo.Serializer());
    }

    @Override
    protected Type getType()
    {
        return new TypeToken<ArrayList<BiomeGenerationInfo>>(){}.getType();
    }
}
