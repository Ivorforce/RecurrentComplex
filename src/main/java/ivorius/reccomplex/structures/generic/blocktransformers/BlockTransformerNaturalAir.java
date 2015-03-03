/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.blocktransformers;

import com.google.gson.*;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.blocks.RCBlocks;
import ivorius.reccomplex.gui.editstructure.blocktransformers.TableDataSourceBTNaturalAir;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class BlockTransformerNaturalAir extends BlockTransformerSingle
{
    public static final double NATURAL_EXPANSION_DISTANCE = 4.0;
    public static final double NATURAL_DISTANCE_RANDOMIZATION = 10.0;

    public BlockMatcher sourceMatcher;

    public BlockTransformerNaturalAir()
    {
        this(BlockMatcher.of(RCBlocks.negativeSpace, 1));
    }

    public BlockTransformerNaturalAir(String sourceExpression)
    {
        this.sourceMatcher = new BlockMatcher(sourceExpression);
    }

    @Override
    public boolean matches(Block block, int metadata)
    {
        return sourceMatcher.apply(new BlockMatcher.BlockFragment(block, metadata));
    }

    @Override
    public void transformBlock(World world, Random random, Phase phase, BlockCoord coord, Block sourceBlock, int sourceMetadata)
    {
        BiomeGenBase biome = world.getBiomeGenForCoords(coord.x, coord.z);
        Block topBlock = biome.topBlock != null ? biome.topBlock : Blocks.air;
        Block fillerBlock = biome.fillerBlock != null ? biome.fillerBlock : Blocks.air;

        coord = coord.subtract(0, 4, 0);

        int currentY = coord.y;
        List<int[]> currentList = new ArrayList<>();
        List<int[]> nextList = new ArrayList<>();
        nextList.add(new int[]{coord.x, coord.z});

        int worldHeight = world.getHeight();
        while (nextList.size() > 0 && currentY < worldHeight)
        {
            List<int[]> cachedList = currentList;
            currentList = nextList;
            nextList = cachedList;

            while (currentList.size() > 0)
            {
                int[] currentPos = currentList.remove(0);
                int currentX = currentPos[0];
                int currentZ = currentPos[1];
                Block curBlock = world.getBlock(currentX, currentY, currentZ);

                boolean isFoliage = curBlock.isFoliage(world, currentX, currentY, currentZ) || curBlock.getMaterial() == Material.leaves || curBlock.getMaterial() == Material.plants || curBlock.getMaterial() == Material.wood;
                boolean isCommon = curBlock == Blocks.stone || curBlock == Blocks.dirt || curBlock == Blocks.sand || curBlock == Blocks.stained_hardened_clay || curBlock == Blocks.gravel;
                boolean replaceable = currentY == coord.y || curBlock == topBlock || curBlock == fillerBlock || curBlock.isReplaceable(world, currentX, currentY, currentZ)
                        || isCommon || isFoliage;
                if (replaceable)
                {
                    world.setBlockToAir(currentX, currentY, currentZ);
                }

                if (replaceable || curBlock.getMaterial() == Material.air)
                {
                    double distToOrigSQ = IvVecMathHelper.distanceSQ(new double[]{coord.x, coord.y, coord.z}, new double[]{currentX, currentY, currentZ});
                    double add = (random.nextDouble() - random.nextDouble()) * NATURAL_DISTANCE_RANDOMIZATION;
                    distToOrigSQ += add < 0 ? -(add * add) : (add * add);

                    if (distToOrigSQ < NATURAL_EXPANSION_DISTANCE * NATURAL_EXPANSION_DISTANCE)
                    {
                        addIfNew(nextList, currentX, currentZ);
                        addIfNew(nextList, currentX - 1, currentZ);
                        addIfNew(nextList, currentX + 1, currentZ);
                        addIfNew(nextList, currentX, currentZ - 1);
                        addIfNew(nextList, currentX, currentZ + 1);
                    }
                }
            }

            currentY++;
        }
    }

    private void addIfNew(List<int[]> list, int... object)
    {
        if (!list.contains(object))
        {
            list.add(object);
        }
    }

    @Override
    public String getDisplayString()
    {
        return "Natural Air: " + sourceMatcher.getDisplayString();
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTNaturalAir(this);
    }

    @Override
    public boolean generatesInPhase(Phase phase)
    {
        return phase == Phase.BEFORE;
    }

    public static class Serializer implements JsonDeserializer<BlockTransformerNaturalAir>, JsonSerializer<BlockTransformerNaturalAir>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public BlockTransformerNaturalAir deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerNatural");

            String expression = BlockTransformerReplace.Serializer.readLegacyMatcher(jsonObject, "source", "sourceMetadata"); // Legacy
            if (expression == null)
                expression = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "sourceExpression", "");

            return new BlockTransformerNaturalAir(expression);
        }

        @Override
        public JsonElement serialize(BlockTransformerNaturalAir transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());

            return jsonObject;
        }
    }
}
