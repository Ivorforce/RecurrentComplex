/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.blocktransformers;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.gui.editstructure.TableDataSourceBTRuins;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.MCRegistrySpecial;

import java.lang.reflect.Type;

/**
 * Created by lukas on 05.06.14.
 */
public class BTProviderRuins implements BlockTransformerProvider<BlockTransformerRuins>
{
    private Serializer serializer;

    public BTProviderRuins()
    {
        serializer = new Serializer(MCRegistrySpecial.INSTANCE);
    }

    @Override
    public BlockTransformerRuins defaultTransformer()
    {
        return new BlockTransformerRuins(0.0f, 0.9f, 0.3f);
    }

    @Override
    public TableDataSource tableDataSource(BlockTransformerRuins element)
    {
        return new TableDataSourceBTRuins(element);
    }

    @Override
    public JsonSerializer<BlockTransformerRuins> serializer()
    {
        return serializer;
    }

    @Override
    public JsonDeserializer<BlockTransformerRuins> deserializer()
    {
        return serializer;
    }

    public static class Serializer implements JsonDeserializer<BlockTransformerRuins>, JsonSerializer<BlockTransformerRuins>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public BlockTransformerRuins deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonobject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerRuins");

            float minDecay = JsonUtils.getJsonObjectFloatFieldValueOrDefault(jsonobject, "minDecay", 0.0f);
            float maxDecay = JsonUtils.getJsonObjectFloatFieldValueOrDefault(jsonobject, "maxDecay", 0.9f);
            float decayChaos = JsonUtils.getJsonObjectFloatFieldValueOrDefault(jsonobject, "decayChaos", 0.3f);

            return new BlockTransformerRuins(minDecay, maxDecay, decayChaos);
        }

        @Override
        public JsonElement serialize(BlockTransformerRuins transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("minDecay", transformer.minDecay);
            jsonobject.addProperty("maxDecay", transformer.maxDecay);
            jsonobject.addProperty("decayChaos", transformer.decayChaos);

            return jsonobject;
        }
    }
}
