/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.placement;

import com.google.gson.*;
import ivorius.reccomplex.json.SerializableStringTypeRegistry;

/**
 * Created by lukas on 19.09.16.
 */
public class FactorRegistry
{
    public static final FactorRegistry INSTANCE = new FactorRegistry();

    private SerializableStringTypeRegistry<GenericPlacer.Factor> factorRegistry = new SerializableStringTypeRegistry<>("factor", "type", GenericPlacer.Factor.class);

    public final Gson gson = createGson();

    private Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(GenericPlacer.class, new GenericPlacer.Serializer());
        factorRegistry.constructGson(builder);
        return builder.create();
    }

    public SerializableStringTypeRegistry<GenericPlacer.Factor> getTypeRegistry()
    {
        return factorRegistry;
    }

}
