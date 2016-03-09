/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import com.google.gson.*;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.blocks.RCBlocks;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTNatural;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.registry.MCRegistrySpecial;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import ivorius.reccomplex.utils.NBTNone;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerNatural extends TransformerSingleBlock<NBTNone>
{
    public static final double DEFAULT_NATURAL_EXPANSION_DISTANCE = 4.0;
    public static final double DEFAULT_NATURAL_EXPANSION_RANDOMIZATION = 6.0;

    public BlockMatcher sourceMatcher;

    public double naturalExpansionDistance;
    public double naturalExpansionRandomization;

    public TransformerNatural()
    {
        this(BlockMatcher.of(MCRegistrySpecial.INSTANCE, RCBlocks.genericSolid, 0), DEFAULT_NATURAL_EXPANSION_DISTANCE, DEFAULT_NATURAL_EXPANSION_RANDOMIZATION);
    }

    public TransformerNatural(String sourceMatcherExpression, double naturalExpansionDistance, double naturalExpansionRandomization)
    {
        this.sourceMatcher = new BlockMatcher(MCRegistrySpecial.INSTANCE, sourceMatcherExpression);
        this.naturalExpansionDistance = naturalExpansionDistance;
        this.naturalExpansionRandomization = naturalExpansionRandomization;
    }

    @Override
    public boolean matches(NBTNone instanceData, Block block, int metadata)
    {
        return sourceMatcher.apply(new BlockMatcher.BlockFragment(block, metadata));
    }

    @Override
    public void transformBlock(NBTNone instanceData, Phase phase, StructureSpawnContext context, BlockCoord coord, Block sourceBlock, int sourceMetadata)
    {
        // TODO Fix for partial generation
        World world = context.world;
        Random random = context.random;

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
                        context.setBlock(currentX, currentY, currentZ, setBlock, 0);
                    }

                    // Uncommenting makes performance shit
                    if (replaceable/* || curBlock == topBlock || curBlock == fillerBlock || curBlock == mainBlock*/)
                    {
                        double yForDistance = coord.y * 0.3 + currentY * 0.7;
                        double distToOrigSQ = IvVecMathHelper.distanceSQ(new double[]{coord.x, coord.y, coord.z}, new double[]{currentX, yForDistance, currentZ});
                        double add = (random.nextDouble() - random.nextDouble()) * naturalExpansionRandomization;
                        distToOrigSQ += add < 0 ? -(add * add) : (add * add);

                        if (distToOrigSQ < naturalExpansionDistance * naturalExpansionDistance)
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
            context.setBlock(coord.x, coord.y, coord.z, setBlock, 0);
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
    public boolean generatesInPhase(NBTNone instanceData, Phase phase)
    {
        return phase == Phase.BEFORE;
    }

    @Override
    public NBTNone prepareInstanceData(StructurePrepareContext context)
    {
        return new NBTNone();
    }

    @Override
    public NBTNone loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return new NBTNone();
    }

    @Override
    public String getDisplayString()
    {
        return "Natural: " + sourceMatcher.getDisplayString();
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTNatural(this);
    }

    public static class Serializer implements JsonDeserializer<TransformerNatural>, JsonSerializer<TransformerNatural>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public TransformerNatural deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.getJsonElementAsJsonObject(jsonElement, "transformerNatural");

            String expression = TransformerReplace.Serializer.readLegacyMatcher(jsonObject, "source", "sourceMetadata"); // Legacy
            if (expression == null)
                expression = JsonUtils.getJsonObjectStringFieldValueOrDefault(jsonObject, "sourceExpression", "");

            double naturalExpansionDistance = JsonUtils.getJsonObjectDoubleFieldValueOrDefault(jsonObject, "naturalExpansionDistance", DEFAULT_NATURAL_EXPANSION_DISTANCE);
            double naturalExpansionRandomization = JsonUtils.getJsonObjectDoubleFieldValueOrDefault(jsonObject, "naturalExpansionRandomization", DEFAULT_NATURAL_EXPANSION_RANDOMIZATION);

            return new TransformerNatural(expression, naturalExpansionDistance, naturalExpansionRandomization);
        }

        @Override
        public JsonElement serialize(TransformerNatural transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());

            jsonObject.addProperty("naturalExpansionDistance", transformer.naturalExpansionDistance);
            jsonObject.addProperty("naturalExpansionRandomization", transformer.naturalExpansionRandomization);

            return jsonObject;
        }
    }
}
