/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.presets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ivorius.reccomplex.structures.generic.DimensionGenerationInfo;
import ivorius.reccomplex.utils.ListPresets;

/**
 * Created by lukas on 26.02.15.
 */
public class DimensionMatcherPresets extends ListPresets<DimensionGenerationInfo>
{
    public static final String FILE_SUFFIX = "rcdp";

    private static DimensionMatcherPresets instance;

    public DimensionMatcherPresets(String fileSuffix)
    {
        super(fileSuffix);
    }

    public static DimensionMatcherPresets instance()
    {
        return instance != null ? instance : (instance = new DimensionMatcherPresets(FILE_SUFFIX));
    }

    @Override
    protected Gson createGson()
    {
        return new GsonBuilder().registerTypeAdapter(DimensionGenerationInfo.class, new DimensionGenerationInfo.Serializer()).create();
    }

    @Override
    protected Class<DimensionGenerationInfo[]> getType()
    {
        return DimensionGenerationInfo[].class;
    }
}
