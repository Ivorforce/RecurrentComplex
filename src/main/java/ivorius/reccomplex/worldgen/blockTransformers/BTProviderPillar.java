/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.blockTransformers;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.gui.editstructure.TableDataSourceBTPillar;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.worldgen.MCRegistrySpecial;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import ivorius.reccomplex.json.JsonUtils;


import java.lang.reflect.Type;

/**
 * Created by lukas on 05.06.14.
 */
public class BTProviderPillar implements BlockTransformerProvider<BlockTransformerPillar>
{
    private Serializer serializer;

    public BTProviderPillar()
    {
        serializer = new Serializer(MCRegistrySpecial.INSTANCE);
    }

    @Override
    public BlockTransformerPillar defaultTransformer()
    {
        return new BlockTransformerPillar(Blocks.stone, 0, Blocks.stone, 0);
    }

    @Override
    public TableDataSource tableDataSource(BlockTransformerPillar element)
    {
        return new TableDataSourceBTPillar(element);
    }

    @Override
    public JsonSerializer<BlockTransformerPillar> serializer()
    {
        return serializer;
    }

    @Override
    public JsonDeserializer<BlockTransformerPillar> deserializer()
    {
        return serializer;
    }

    public static class Serializer implements JsonDeserializer<BlockTransformerPillar>, JsonSerializer<BlockTransformerPillar>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public BlockTransformerPillar deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonobject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerPillar");

            String sourceBlock = JsonUtils.getJsonObjectStringFieldValue(jsonobject, "source");
            Block source = registry.blockFromID(sourceBlock);
            int sourceMeta = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonobject, "sourceMetadata", -1);

            String destBlock = JsonUtils.getJsonObjectStringFieldValue(jsonobject, "dest");
            Block dest = registry.blockFromID(destBlock);
            int destMeta = JsonUtils.getJsonObjectIntegerFieldValue(jsonobject, "destMetadata");

            return new BlockTransformerPillar(source, sourceMeta, dest, destMeta);
        }

        @Override
        public JsonElement serialize(BlockTransformerPillar transformerPillar, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonobject = new JsonObject();

            jsonobject.addProperty("source", Block.blockRegistry.getNameForObject(transformerPillar.sourceBlock));
            if (transformerPillar.sourceMetadata >= 0)
            {
                jsonobject.addProperty("sourceMetadata", transformerPillar.sourceMetadata);
            }

            jsonobject.addProperty("dest", Block.blockRegistry.getNameForObject(transformerPillar.destBlock));
            jsonobject.addProperty("destMetadata", transformerPillar.destMetadata);

            return jsonobject;
        }
    }
}
