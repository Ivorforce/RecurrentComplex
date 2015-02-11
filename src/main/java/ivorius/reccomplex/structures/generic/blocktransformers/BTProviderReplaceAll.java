/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.blocktransformers;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.gui.editstructure.TableDataSourceBTReplaceAll;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.MCRegistrySpecial;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.lang.reflect.Type;

/**
 * Created by lukas on 05.06.14.
 */
public class BTProviderReplaceAll implements BlockTransformerProvider<BlockTransformerReplaceAll>
{
    private Serializer serializer;

    public BTProviderReplaceAll()
    {
        serializer = new Serializer(MCRegistrySpecial.INSTANCE);
    }

    @Override
    public BlockTransformerReplaceAll defaultTransformer()
    {
        return new BlockTransformerReplaceAll(Blocks.wool, 0, Blocks.wool, new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
    }

    @Override
    public TableDataSource tableDataSource(BlockTransformerReplaceAll element)
    {
        return new TableDataSourceBTReplaceAll(element);
    }

    @Override
    public JsonSerializer<BlockTransformerReplaceAll> serializer()
    {
        return serializer;
    }

    @Override
    public JsonDeserializer<BlockTransformerReplaceAll> deserializer()
    {
        return serializer;
    }

    public static class Serializer implements JsonDeserializer<BlockTransformerReplaceAll>, JsonSerializer<BlockTransformerReplaceAll>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public BlockTransformerReplaceAll deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonobject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerReplace");

            String sourceBlock = JsonUtils.getJsonObjectStringFieldValue(jsonobject, "source");
            Block source = registry.blockFromID(sourceBlock);
            int sourceMeta = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonobject, "sourceMetadata", -1);

            String destBlock = JsonUtils.getJsonObjectStringFieldValue(jsonobject, "dest");
            Block dest = registry.blockFromID(destBlock);
            byte[] destMeta = context.deserialize(jsonobject.get("destMetadata"), byte[].class);

            return new BlockTransformerReplaceAll(source, sourceMeta, dest, destMeta);
        }

        @Override
        public JsonElement serialize(BlockTransformerReplaceAll transformerPillar, Type par2Type, JsonSerializationContext context)
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
