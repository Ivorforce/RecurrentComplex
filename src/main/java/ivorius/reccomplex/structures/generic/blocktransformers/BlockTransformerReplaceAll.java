/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.blocktransformers;

import com.google.gson.*;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.gui.editstructure.blocktransformers.TableDataSourceBTReplaceAll;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class BlockTransformerReplaceAll implements BlockTransformer
{
    public BlockMatcher sourceMatcher;

    public Block destBlock;
    public byte[] destMetadata;

    public BlockTransformerReplaceAll()
    {
        this(BlockMatcher.of(Blocks.wool, new IntegerRange(0, 15)), Blocks.wool, new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
    }

    public BlockTransformerReplaceAll(String sourceExpression, Block destBlock, byte[] destMetadata)
    {
        this.sourceMatcher = new BlockMatcher(sourceExpression);
        this.destBlock = destBlock;
        this.destMetadata = destMetadata;
    }

    @Override
    public boolean skipGeneration(Block block, int metadata)
    {
        return sourceMatcher.apply(new BlockMatcher.BlockFragment(block, metadata));
    }

    @Override
    public void transform(Phase phase, StructureSpawnContext context, IvWorldData worldData, List<BlockTransformer> transformerList)
    {
        IvBlockCollection blockCollection = worldData.blockCollection;

        byte destMeta = destMetadata[context.random.nextInt(destMetadata.length)];
        int[] areaSize = new int[]{blockCollection.width, blockCollection.height, blockCollection.length};
        BlockCoord lowerCoord = context.lowerCoord();

        for (BlockCoord sourceCoord : blockCollection)
        {
            BlockCoord worldCoord = context.transform.apply(sourceCoord, areaSize).add(lowerCoord);

            Block block = blockCollection.getBlock(sourceCoord);
            int meta = blockCollection.getMetadata(sourceCoord);

            if (skipGeneration(block, meta))
                context.world.setBlock(worldCoord.x, worldCoord.y, worldCoord.z, destBlock, destMeta, 3);
        }
    }

    @Override
    public String getDisplayString()
    {
        return "Replace All: " + sourceMatcher.getDisplayString() + "->" + destBlock.getLocalizedName();
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTReplaceAll(this);
    }

    @Override
    public boolean generatesInPhase(Phase phase)
    {
        return phase == Phase.BEFORE;
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
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerReplace");

            String expression = BlockTransformerReplace.Serializer.readLegacyMatcher(jsonObject, "source", "sourceMetadata"); // Legacy
            if (expression == null)
                expression = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "sourceExpression", "");

            String destBlock = JsonUtils.getJsonObjectStringFieldValue(jsonObject, "dest");
            Block dest = registry.blockFromID(destBlock);
            byte[] destMeta = context.deserialize(jsonObject.get("destMetadata"), byte[].class);

            return new BlockTransformerReplaceAll(expression, dest, destMeta);
        }

        @Override
        public JsonElement serialize(BlockTransformerReplaceAll transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());

            jsonObject.addProperty("dest", Block.blockRegistry.getNameForObject(transformer.destBlock));
            jsonObject.add("destMetadata", context.serialize(transformer.destMetadata, byte[].class));

            return jsonObject;
        }
    }
}
