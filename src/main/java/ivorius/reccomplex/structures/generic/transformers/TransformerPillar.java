/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import com.google.gson.*;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTPillar;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.lang.reflect.Type;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerPillar extends TransformerSingleBlock
{
    public BlockMatcher sourceMatcher;

    public Block destBlock;
    public int destMetadata;

    public TransformerPillar()
    {
        this(BlockMatcher.of(Blocks.stone, 0), Blocks.stone, 0);
    }

    public TransformerPillar(String sourceExpression, Block destBlock, int destMetadata)
    {
        this.sourceMatcher = new BlockMatcher(sourceExpression);
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
        world.setBlock(coord.x, coord.y, coord.z, destBlock, destMetadata, 3);
        int y = coord.y;
        y--;

        while (y > 0)
        {
            Block block = world.getBlock(coord.x, y, coord.z);

            if (!(block.isReplaceable(world, coord.x, y, coord.z) || block.getMaterial() == Material.leaves || block.isFoliage(world, coord.x, y, coord.z)))
            {
                return;
            }

            world.setBlock(coord.x, y, coord.z, destBlock, destMetadata, 3);
            y--;
        }
    }

    @Override
    public String getDisplayString()
    {
        return "Pillar: " + sourceMatcher.getDisplayString() + "->" + destBlock.getLocalizedName();
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTPillar(this);
    }

    @Override
    public boolean generatesInPhase(Phase phase)
    {
        return phase == Phase.BEFORE;
    }

    public static class Serializer implements JsonDeserializer<TransformerPillar>, JsonSerializer<TransformerPillar>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public TransformerPillar deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerPillar");

            String expression = TransformerReplace.Serializer.readLegacyMatcher(jsonObject, "source", "sourceMetadata"); // Legacy
            if (expression == null)
                expression = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "sourceExpression", "");

            String destBlock = JsonUtils.getJsonObjectStringFieldValue(jsonObject, "dest");
            Block dest = registry.blockFromID(destBlock);
            int destMeta = JsonUtils.getJsonObjectIntegerFieldValue(jsonObject, "destMetadata");

            return new TransformerPillar(expression, dest, destMeta);
        }

        @Override
        public JsonElement serialize(TransformerPillar transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());

            jsonObject.addProperty("dest", Block.blockRegistry.getNameForObject(transformer.destBlock));
            jsonObject.addProperty("destMetadata", transformer.destMetadata);

            return jsonObject;
        }
    }
}
