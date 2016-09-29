/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.presets;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ivorius.reccomplex.structures.generic.DimensionGenerationInfo;
import ivorius.reccomplex.utils.PresetRegistry;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by lukas on 26.02.15.
 */
public class DimensionMatcherPresets extends PresetRegistry<ArrayList<DimensionGenerationInfo>>
{
    public static final String FILE_SUFFIX = "rcdp";

    private static DimensionMatcherPresets instance;

    public DimensionMatcherPresets(String fileSuffix)
    {
        super(fileSuffix, "dimension preset");
    }

    public static DimensionMatcherPresets instance()
    {
        return instance != null ? instance : (instance = new DimensionMatcherPresets(FILE_SUFFIX));
    }

    @Override
    protected void registerGson(GsonBuilder builder)
    {
        builder.registerTypeAdapter(DimensionGenerationInfo.class, new DimensionGenerationInfo.Serializer());
    }

    @Override
    protected Type getType()
    {
        return new TypeToken<ArrayList<DimensionGenerationInfo>>(){}.getType();
    }
}
