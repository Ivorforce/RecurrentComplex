/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.presets;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import ivorius.reccomplex.files.loading.RCFileSuffix;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.GenericPlacer;
import ivorius.reccomplex.utils.presets.PresetRegistry;

import java.lang.reflect.Type;

/**
 * Created by lukas on 26.02.15.
 */
public class GenericPlacerPresets extends PresetRegistry<GenericPlacer>
{

    private static GenericPlacerPresets instance;

    public GenericPlacerPresets(String fileSuffix)
    {
        super(fileSuffix, "placer preset");
    }

    public static GenericPlacerPresets instance()
    {
        return instance != null ? instance : (instance = new GenericPlacerPresets(RCFileSuffix.PLACER_PRESET));
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
