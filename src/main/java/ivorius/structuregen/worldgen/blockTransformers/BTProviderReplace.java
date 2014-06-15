/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.worldgen.blockTransformers;

import com.google.gson.*;
import ivorius.structuregen.gui.editstructure.TableDataSourceBTReplace;
import ivorius.structuregen.gui.table.TableDataSource;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.JsonUtils;

import java.lang.reflect.Type;

/**
 * Created by lukas on 05.06.14.
 */
public class BTProviderReplace implements BlockTransformerProvider<BlockTransformerReplace>
{
    private Serializer serializer;

    public BTProviderReplace()
    {
        serializer = new Serializer();
    }

    @Override
    public BlockTransformerReplace defaultTransformer()
    {
        return new BlockTransformerReplace(Blocks.wool, 0, Blocks.wool, new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
    }

    @Override
    public TableDataSource tableDataSource(BlockTransformerReplace element)
    {
        return new TableDataSourceBTReplace(element);
    }

    @Override
    public JsonSerializer<BlockTransformerReplace> serializer()
    {
        return serializer;
    }

    @Override
    public JsonDeserializer<BlockTransformerReplace> deserializer()
    {
        return serializer;
    }

    public static class Serializer implements JsonDeserializer<BlockTransformerReplace>, JsonSerializer<BlockTransformerReplace>
    {
        @Override
        public BlockTransformerReplace deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonobject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerReplace");

            String sourceBlock = JsonUtils.getJsonObjectStringFieldValue(jsonobject, "source");
            Block source = (Block) Block.blockRegistry.getObject(sourceBlock);
            int sourceMeta = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonobject, "sourceMetadata", -1);

            String destBlock = JsonUtils.getJsonObjectStringFieldValue(jsonobject, "dest");
            Block dest = (Block) Block.blockRegistry.getObject(destBlock);
            byte[] destMeta = context.deserialize(jsonobject.get("destMetadata"), byte[].class);

            return new BlockTransformerReplace(source, sourceMeta, dest, destMeta);
        }

        @Override
        public JsonElement serialize(BlockTransformerReplace transformerPillar, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("source", Block.blockRegistry.getNameForObject(transformerPillar.sourceBlock));
            if (transformerPillar.sourceMetadata >= 0)
            {
                jsonobject.addProperty("sourceMetadata", transformerPillar.sourceMetadata);
            }

            jsonobject.addProperty("dest", Block.blockRegistry.getNameForObject(transformerPillar.destBlock));
            jsonobject.add("destMetadata", context.serialize(transformerPillar.destMetadata, byte[].class));

            return jsonobject;
        }
    }
}
