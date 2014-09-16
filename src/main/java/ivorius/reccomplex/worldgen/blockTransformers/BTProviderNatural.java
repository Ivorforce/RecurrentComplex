/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.blockTransformers;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.gui.editstructure.TableDataSourceBTNatural;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.worldgen.MCRegistrySpecial;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import ivorius.reccomplex.json.JsonUtils;


import java.lang.reflect.Type;

/**
 * Created by lukas on 05.06.14.
 */
public class BTProviderNatural implements BlockTransformerProvider<BlockTransformerNatural>
{
    private Serializer serializer;

    public BTProviderNatural()
    {
        serializer = new Serializer(MCRegistrySpecial.INSTANCE);
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
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public BlockTransformerNatural deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonobject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerNatural");

            String sourceBlock = JsonUtils.getJsonObjectStringFieldValue(jsonobject, "source");
            Block source = registry.blockFromID(sourceBlock);
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
