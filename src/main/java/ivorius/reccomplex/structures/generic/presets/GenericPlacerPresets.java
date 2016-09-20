/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.presets;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import ivorius.reccomplex.structures.generic.placement.GenericPlacer;
import ivorius.reccomplex.utils.PresetRegistry;

import java.lang.reflect.Type;

/**
 * Created by lukas on 26.02.15.
 */
public class GenericPlacerPresets extends PresetRegistry<GenericPlacer>
{
    public static final String FILE_SUFFIX = "rcgp";

    private static GenericPlacerPresets instance;

    public GenericPlacerPresets(String fileSuffix)
    {
        super(fileSuffix);
    }

    public static GenericPlacerPresets instance()
    {
        return instance != null ? instance : (instance = new GenericPlacerPresets(FILE_SUFFIX));
    }

    @Override
    protected void registerGson(GsonBuilder builder)
    {
        builder.registerTypeAdapter(GenericPlacer.class, new GenericPlacer.Serializer());
    }

    @Override
    protected Type getType()
    {
        return new TypeToken<GenericPlacer>(){}.getType();
    }
}
