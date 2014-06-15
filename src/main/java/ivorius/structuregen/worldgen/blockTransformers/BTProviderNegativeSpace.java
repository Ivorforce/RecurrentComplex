package ivorius.structuregen.worldgen.blockTransformers;

import com.google.gson.*;
import ivorius.structuregen.blocks.SGBlocks;
import ivorius.structuregen.gui.editstructure.TableDataSourceBTNegativeSpace;
import ivorius.structuregen.gui.table.TableDataSource;
import net.minecraft.block.Block;
import net.minecraft.util.JsonUtils;

import java.lang.reflect.Type;

/**
 * Created by lukas on 05.06.14.
 */
public class BTProviderNegativeSpace implements BlockTransformerProvider<BlockTransformerNegativeSpace>
{
    private Serializer serializer;

    public BTProviderNegativeSpace()
    {
        serializer = new Serializer();
    }

    @Override
    public BlockTransformerNegativeSpace defaultTransformer()
    {
        return new BlockTransformerNegativeSpace(SGBlocks.negativeSpace, 0);
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
        @Override
        public BlockTransformerNegativeSpace deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonobject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerNegativeSpace");

            String sourceBlock = JsonUtils.getJsonObjectStringFieldValue(jsonobject, "source");
            Block source = (Block) Block.blockRegistry.getObject(sourceBlock);
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
