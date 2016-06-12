/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import com.google.gson.*;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.blocks.RCBlocks;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTNatural;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.matchers.BlockMatcher;
import ivorius.reccomplex.utils.NBTNone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.BlockPos;
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
        this(BlockMatcher.of(RecurrentComplex.specialRegistry, RCBlocks.genericSolid, 0), DEFAULT_NATURAL_EXPANSION_DISTANCE, DEFAULT_NATURAL_EXPANSION_RANDOMIZATION);
    }

    public TransformerNatural(String sourceMatcherExpression, double naturalExpansionDistance, double naturalExpansionRandomization)
    {
        this.sourceMatcher = new BlockMatcher(RecurrentComplex.specialRegistry, sourceMatcherExpression);
        this.naturalExpansionDistance = naturalExpansionDistance;
        this.naturalExpansionRandomization = naturalExpansionRandomization;
    }

    public static void addIfNew(List<int[]> list, int... object)
    {
        if (!list.contains(object))
            list.add(object);
    }

    @Override
    public boolean matches(NBTNone instanceData, IBlockState state)
    {
        return sourceMatcher.apply(state);
    }

    @Override
    public void transformBlock(NBTNone instanceData, Phase phase, StructureSpawnContext context, BlockPos coord, IBlockState sourceState)
    {
        // TODO Fix for partial generation
        World world = context.world;
        Random random = context.random;

        BiomeGenBase biome = world.getBiomeGenForCoords(coord);
        IBlockState topBlock = biome.topBlock != null ? biome.topBlock : Blocks.air.getDefaultState();
        IBlockState fillerBlock = biome.fillerBlock != null ? biome.fillerBlock : Blocks.air.getDefaultState();
        IBlockState mainBlock = world.provider.getDimensionId() == -1 ? Blocks.netherrack.getDefaultState() : (world.provider.getDimensionId() == 1 ? Blocks.end_stone.getDefaultState() : Blocks.stone.getDefaultState());

        boolean useStoneBlock = hasBlockAbove(world, coord, mainBlock);

        if (phase == Phase.BEFORE)
        {
            int currentY = coord.getY();
            List<int[]> currentList = new ArrayList<>();
            List<int[]> nextList = new ArrayList<>();
            nextList.add(new int[]{coord.getX(), coord.getZ()});

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

                    BlockPos curBlockPos = new BlockPos(currentX, currentY, currentZ);
                    IBlockState curBlock = world.getBlockState(curBlockPos);

                    boolean replaceable = currentY == coord.getY() || curBlock.getBlock().isReplaceable(world, curBlockPos);
                    if (replaceable)
                    {
                        IBlockState setBlock = useStoneBlock ? mainBlock : (isTopBlock(world, curBlockPos) ? topBlock : fillerBlock);
                        context.setBlock(curBlockPos, setBlock, 2);
                    }

                    // Uncommenting makes performance shit
                    if (replaceable/* || curBlock == topBlock || curBlock == fillerBlock || curBlock == mainBlock*/)
                    {
                        double yForDistance = coord.getY() * 0.3 + currentY * 0.7;
                        double distToOrigSQ = IvVecMathHelper.distanceSQ(new double[]{coord.getX(), coord.getY(), coord.getZ()}, new double[]{currentX, yForDistance, currentZ});
                        double add = (random.nextDouble() - random.nextDouble()) * naturalExpansionRandomization;
                        distToOrigSQ += add < 0 ? -(add * add) : (add * add);

                        if (distToOrigSQ < naturalExpansionDistance * naturalExpansionDistance)
                            addNewNeighbors(nextList, currentX, currentZ);
                    }
                }

                currentY--;
            }
        }
        else
        {
            // Get the top blocks right (grass rather than dirt)
            IBlockState setBlock = useStoneBlock ? mainBlock : (isTopBlock(world, coord) ? topBlock : fillerBlock);
            context.setBlock(coord, setBlock, 2);
        }
    }

    public static void addNewNeighbors(List<int[]> list, int x, int z)
    {
        addIfNew(list, x, z);
        addIfNew(list, x - 1, z);
        addIfNew(list, x + 1, z);
        addIfNew(list, x, z - 1);
        addIfNew(list, x, z + 1);
    }

    private boolean hasBlockAbove(World world, BlockPos pos, IBlockState blockType)
    {
        int y = pos.getY();
        for (; y < world.getHeight() && y < pos.getY() + 60; y++)
        {
            if (world.getBlockState(new BlockPos(pos.getX(), y, pos.getZ())) == blockType)
                return true;
        }

        return false;
    }

    private boolean isTopBlock(World world, BlockPos pos)
    {
        return !world.isBlockNormalCube(pos.up(), false);
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
