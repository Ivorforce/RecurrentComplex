/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.blocktransformers;

import com.google.gson.*;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.gui.editstructure.blocktransformers.TableDataSourceBTPillar;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.lang.reflect.Type;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class BlockTransformerPillar extends BlockTransformerSingle
{
    public Block sourceBlock;
    public int sourceMetadata;

    public Block destBlock;
    public int destMetadata;

    public BlockTransformerPillar()
    {
        this(Blocks.stone, 0, Blocks.stone, 0);
    }

    public BlockTransformerPillar(Block sourceBlock, int sourceMetadata, Block destBlock, int destMetadata)
    {
        this.sourceBlock = sourceBlock;
        this.sourceMetadata = sourceMetadata;
        this.destBlock = destBlock;
        this.destMetadata = destMetadata;
    }

    @Override
    public boolean matches(Block block, int metadata)
    {
        return block == sourceBlock && (metadata < 0 || metadata == sourceMetadata);
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
    public String displayString()
    {
        return "Pillar: " + sourceBlock.getLocalizedName() + "->" + destBlock.getLocalizedName();
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
