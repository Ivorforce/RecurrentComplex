/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.worldgen.blockTransformers;

import com.google.gson.*;
import ivorius.structuregen.gui.editstructure.TableDataSourceBTNatural;
import ivorius.structuregen.gui.table.TableDataSource;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.JsonUtils;

import java.lang.reflect.Type;

/**
 * Created by lukas on 05.06.14.
 */
public class BTProviderNatural implements BlockTransformerProvider<BlockTransformerNatural>
{
    private Serializer serializer;

    public BTProviderNatural()
    {
        serializer = new Serializer();
    }

    @Override
    public BlockTransformerNatural defaultTransformer()
    {
        return new BlockTransformerNatural(Blocks.grass, 0);
    }

    @Override
    public TableDataSource tableDataSource(BlockTransformerNatural element)
    {
        return new TableDataSourceBTNatural(element);
    }

    @Override
    public JsonSerializer<BlockTransformerNatural> serializer()
    {
        return serializer;
    }

    @Override
    public JsonDeserializer<BlockTransformerNatural> deserializer()
    {
        return serializer;
    }

    public static class Serializer implements JsonDeserializer<BlockTransformerNatural>, JsonSerializer<BlockTransformerNatural>
    {
        @Override
        public BlockTransformerNatural deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonobject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerNatural");

            String sourceBlock = JsonUtils.getJsonObjectStringFieldValue(jsonobject, "source");
            Block source = (Block) Block.blockRegistry.getObject(sourceBlock);
            int sourceMeta = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonobject, "sourceMetadata", -1);

            return new BlockTransformerNatural(source, sourceMeta);
        }

        @Override
        public JsonElement serialize(BlockTransformerNatural transformerPillar, Type par2Type, JsonSerializationContext context)
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
