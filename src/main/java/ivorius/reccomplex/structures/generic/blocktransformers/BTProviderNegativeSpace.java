/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.blocktransformers;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.blocks.RCBlocks;
import ivorius.reccomplex.gui.editstructure.TableDataSourceBTNegativeSpace;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.structures.MCRegistrySpecial;
import net.minecraft.block.Block;
import ivorius.reccomplex.json.JsonUtils;


import java.lang.reflect.Type;

/**
 * Created by lukas on 05.06.14.
 */
public class BTProviderNegativeSpace implements BlockTransformerProvider<BlockTransformerNegativeSpace>
{
    private Serializer serializer;

    public BTProviderNegativeSpace()
    {
        serializer = new Serializer(MCRegistrySpecial.INSTANCE);
    }

    @Override
    public BlockTransformerNegativeSpace defaultTransformer()
    {
        return new BlockTransformerNegativeSpace(RCBlocks.negativeSpace, 0);
    }

    @Override
    public TableDataSource tableDataSource(BlockTransformerNegativeSpace element)
    {
        return new TableDataSourceBTNegativeSpace(element);
    }

    @Override
    public JsonSerializer<BlockTransformerNegativeSpace> serializer()
    {
        return serializer;
    }

    @Override
    public JsonDeserializer<BlockTransformerNegativeSpace> deserializer()
    {
        return serializer;
    }

    public static class Serializer implements JsonDeserializer<BlockTransformerNegativeSpace>, JsonSerializer<BlockTransformerNegativeSpace>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public BlockTransformerNegativeSpace deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonobject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerNegativeSpace");

            String sourceBlock = JsonUtils.getJsonObjectStringFieldValue(jsonobject, "source");
            Block source = registry.blockFromID(sourceBlock);
            int sourceMeta = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonobject, "sourceMetadata", -1);

            return new BlockTransformerNegativeSpace(source, sourceMeta);
        }

        @Override
        public JsonElement serialize(BlockTransformerNegativeSpace transformerPillar, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("source", Block.blockRegistry.getNameForObject(transformerPillar.sourceBlock));
            if (transformerPillar.sourceMetadata >= 0)
            {
                jsonobject.addProperty("sourceMetadata", transformerPillar.sourceMetadata);
            }

            return jsonobject;
        }
    }
}
