/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.blocktransformers;

import com.google.gson.*;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.gui.editstructure.blocktransformers.TableDataSourceBTReplace;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.lang.reflect.Type;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class BlockTransformerReplace extends BlockTransformerSingle
{
    public BlockMatcher sourceMatcher;

    public Block destBlock;
    public byte[] destMetadata;

    public BlockTransformerReplace()
    {
        this(BlockMatcher.of(Blocks.wool, new IntegerRange(0, 15)),
                Blocks.wool, new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
    }

    public BlockTransformerReplace(String sourceMatcherExpression, Block destBlock, byte[] destMetadata)
    {
        this.sourceMatcher = new BlockMatcher(sourceMatcherExpression);
        this.destBlock = destBlock;
        this.destMetadata = destMetadata;
    }

    @Override
    public boolean matches(Block block, int metadata)
    {
        return sourceMatcher.apply(new BlockMatcher.BlockFragment(block, metadata));
    }

    @Override
    public void transformBlock(World world, Random random, Phase phase, BlockCoord coord, Block sourceBlock, int sourceMetadata)
    {
        world.setBlock(coord.x, coord.y, coord.z, destBlock, destMetadata[random.nextInt(destMetadata.length)], 3);
    }

    @Override
    public String getDisplayString()
    {
        return "Replace: " + sourceMatcher.getDisplayString();
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTReplace(this);
    }

    @Override
    public boolean generatesInPhase(Phase phase)
    {
        return phase == Phase.BEFORE;
    }

    public static class Serializer implements JsonDeserializer<BlockTransformerReplace>, JsonSerializer<BlockTransformerReplace>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        public static String readLegacyMatcher(JsonObject jsonObject, String blockKey, String metadataKey)
        {
            if (jsonObject.has(blockKey))
            {
                String sourceBlock = JsonUtils.getJsonObjectStringFieldValue(jsonObject, blockKey);
                int sourceMeta = JsonUtils.getJsonObjectIntegerFieldValueOrDefault(jsonObject, metadataKey, -1);
                return sourceMeta >= 0 ? String.format("%s & %s%d", sourceBlock, BlockMatcher.METADATA_PREFIX, sourceMeta) : sourceBlock;
            }

            return null;
        }

        @Override
        public BlockTransformerReplace deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerReplace");

            String expression = readLegacyMatcher(jsonObject, "source", "sourceMetadata"); // Legacy
            if (expression == null)
                expression = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "sourceExpression", "");

            String destBlock = JsonUtils.getJsonObjectStringFieldValue(jsonObject, "dest");
            Block dest = registry.blockFromID(destBlock);
            byte[] destMeta = context.deserialize(jsonObject.get("destMetadata"), byte[].class);

            return new BlockTransformerReplace(expression, dest, destMeta);
        }

        @Override
        public JsonElement serialize(BlockTransformerReplace transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());

            jsonObject.addProperty("dest", Block.blockRegistry.getNameForObject(transformer.destBlock));
            jsonObject.add("destMetadata", context.serialize(transformer.destMetadata, byte[].class));

            return jsonObject;
        }
    }
}
