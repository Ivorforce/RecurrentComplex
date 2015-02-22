/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.blocktransformers;

import com.google.gson.*;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.gui.editstructure.blocktransformers.TableDataSourceBTNatural;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import net.minecraft.block.Block;
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
public class BlockTransformerNatural extends BlockTransformerSingle
{
    public static final double NATURAL_EXPANSION_DISTANCE = 4.0;
    public static final double NATURAL_DISTANCE_RANDOMIZATION = 6.0;

    public Block sourceBlock;
    public int sourceMetadata;

    public BlockTransformerNatural()
    {
        this(Blocks.grass, 0);
    }

    public BlockTransformerNatural(Block sourceBlock, int sourceMetadata)
    {
        this.sourceBlock = sourceBlock;
        this.sourceMetadata = sourceMetadata;
    }

    @Override
    public boolean matches(Block block, int metadata)
    {
        return block == sourceBlock && (metadata < 0 || metadata == sourceMetadata);
    }

    @Override
    public void transformBlock(World world, Random random, Phase phase, BlockCoord coord, Block sourceBlock, int sourceMetadata)
    {
        BiomeGenBase biome = world.getBiomeGenForCoords(coord.x, coord.z);
        Block topBlock = biome.topBlock != null ? biome.topBlock : Blocks.air;
        Block fillerBlock = biome.fillerBlock != null ? biome.fillerBlock : Blocks.air;
        Block mainBlock = world.provider.dimensionId == -1 ? Blocks.netherrack : (world.provider.dimensionId == 1 ? Blocks.end_stone : Blocks.stone);

        boolean useStoneBlock = hasBlockAbove(world, coord.x, coord.y, coord.z, mainBlock);

        if (phase == Phase.BEFORE)
        {
            int currentY = coord.y;
            List<int[]> currentList = new ArrayList<>();
            List<int[]> nextList = new ArrayList<>();
            nextList.add(new int[]{coord.x, coord.z});

            while (nextList.size() > 0 && currentY > 1)
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

                    boolean replaceable = currentY == coord.y || curBlock.isReplaceable(world, currentX, currentY, currentZ);
                    if (replaceable)
                    {
                        Block setBlock = useStoneBlock ? mainBlock : (isTopBlock(world, currentX, currentY, currentZ) ? topBlock : fillerBlock);
                        world.setBlock(currentX, currentY, currentZ, setBlock);
                    }

                    // Uncommenting makes performance shit
                    if (replaceable/* || curBlock == topBlock || curBlock == fillerBlock || curBlock == mainBlock*/)
                    {
                        double yForDistance = coord.y * 0.3 + currentY * 0.7;
                        double distToOrigSQ = IvVecMathHelper.distanceSQ(new double[]{coord.x, coord.y, coord.z}, new double[]{currentX, yForDistance, currentZ});
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

                currentY--;
            }
        }
        else
        {
            // Get the top blocks right (grass rather than dirt)
            Block setBlock = useStoneBlock ? mainBlock : (isTopBlock(world, coord.x, coord.y, coord.z) ? topBlock : fillerBlock);
            world.setBlock(coord.x, coord.y, coord.z, setBlock);
        }
    }

    private void addIfNew(List<int[]> list, int... object)
    {
        if (!list.contains(object))
        {
            list.add(object);
        }
    }

    private boolean hasBlockAbove(World world, int x, int y, int z, Block blockType)
    {
        int origY = y;
        for (; y < world.getHeight() && y < origY + 60; y++)
        {
            if (world.getBlock(x, y, z) == blockType)
                return true;
        }

        return false;
    }

    private boolean isTopBlock(World world, int x, int y, int z)
    {
        return !world.isBlockNormalCubeDefault(x, y + 1, z, false);
    }

    @Override
    public boolean generatesInPhase(Phase phase)
    {
        return phase == Phase.BEFORE;
    }

    @Override
    public String displayString()
    {
        return "Natural: " + sourceBlock.getLocalizedName();
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTNatural(this);
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
