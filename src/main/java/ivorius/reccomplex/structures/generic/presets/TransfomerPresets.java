/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.presets;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import ivorius.reccomplex.files.RCFileSuffix;
import ivorius.reccomplex.structures.generic.transformers.TransformerMulti;
import ivorius.reccomplex.utils.PresetRegistry;

import java.lang.reflect.Type;

/**
 * Created by lukas on 26.02.15.
 */
public class TransfomerPresets extends PresetRegistry<TransformerMulti.Data>
{

    private static TransfomerPresets instance;

    public TransfomerPresets(String fileSuffix)
    {
        super(fileSuffix, "transformer preset");
    }

    public static TransfomerPresets instance()
    {
        return instance != null ? instance : (instance = new TransfomerPresets(RCFileSuffix.TRANSFORMER_PRESET));
    }

    @Override
    protected void registerGson(GsonBuilder builder)
    {
        builder.registerTypeAdapter(TransformerMulti.Data.class, new TransformerMulti.DataSerializer());
    }

    @Override
    protected Type getType()
    {
        return new TypeToken<TransformerMulti.Data>(){}.getType();
    }
}
