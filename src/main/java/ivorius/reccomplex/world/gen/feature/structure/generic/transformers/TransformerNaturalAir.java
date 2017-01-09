/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.transformers;

import com.google.gson.*;
import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.block.RCBlocks;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTNaturalAir;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.ivtoolkit.world.chunk.gen.StructureBoundingBoxes;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.expression.PositionedBlockMatcher;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.utils.expression.BlockMatcher;
import ivorius.reccomplex.utils.RCBlockLogic;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerNaturalAir extends TransformerAbstractCloud<TransformerNaturalAir.InstanceData>
{
    public static final double DEFAULT_NATURAL_EXPANSION_DISTANCE = 4.0;
    public static final double DEFAULT_NATURAL_EXPANSION_RANDOMIZATION = 10.0;
    public static final int MAX_TREE_SIZE = 300;

    public BlockMatcher sourceMatcher;
    public PositionedBlockMatcher destMatcher;

    public double naturalExpansionDistance;
    public double naturalExpansionRandomization;

    public TransformerNaturalAir()
    {
        this(null, BlockMatcher.of(RecurrentComplex.specialRegistry, RCBlocks.genericSpace, 1), DEFAULT_NATURAL_EXPANSION_DISTANCE, DEFAULT_NATURAL_EXPANSION_RANDOMIZATION);
    }

    public TransformerNaturalAir(@Nullable String id, String sourceMatcherExpression, double naturalExpansionDistance, double naturalExpansionRandomization)
    {
        super(id != null ? id : randomID(TransformerNaturalAir.class));
        this.sourceMatcher = ExpressionCache.of(new BlockMatcher(RecurrentComplex.specialRegistry), sourceMatcherExpression);
        this.destMatcher = ExpressionCache.of(new PositionedBlockMatcher(RecurrentComplex.specialRegistry), "");
        this.naturalExpansionDistance = naturalExpansionDistance;
        this.naturalExpansionRandomization = naturalExpansionRandomization;
    }

    protected static Stream<BlockPos> neighbors(BlockPos worldPos)
    {
        return BlockAreas.streamPositions(new BlockArea(worldPos.add(-1, -1, -1), worldPos.add(1, 1, 1)));
    }

    @Override
    public boolean canPenetrate(Environment environment, IvWorldData worldData, BlockPos pos, double density, TransformerMulti transformer, TransformerMulti.InstanceData transformerID)
    {
        return destMatcher.evaluate(() -> PositionedBlockMatcher.Argument.at(environment.world, pos));
    }

    @Override
    public boolean matches(InstanceData instanceData, IBlockState state)
    {
        return sourceMatcher.test(state);
    }

    @Override
    public void transformBlock(InstanceData instanceData, Phase phase, StructureSpawnContext context, BlockPos sourcePos, BlockPos worldPos, IBlockState sourceState, double density)
    {
        context.setBlock(worldPos, Blocks.AIR.getDefaultState(), 2);
    }

    @Override
    public void transform(InstanceData instanceData, Phase phase, StructureSpawnContext context, IvWorldData worldData, RunTransformer transformer)
    {
        super.transform(instanceData, phase, context, worldData, transformer);

        if (phase == Phase.AFTER)
        {
            WorldServer world = context.environment.world;
            IvBlockCollection blockCollection = worldData.blockCollection;
            int[] areaSize = new int[]{blockCollection.width, blockCollection.height, blockCollection.length};
            BlockPos lowerCoord = StructureBoundingBoxes.min(context.boundingBox);

            // Remove dying foliage
            HashSet<BlockPos> check = instanceData.cloud.keySet().stream()
                    .flatMap(pos -> new BlockArea(pos.subtract(new Vec3i(2, 2, 2)), pos.add(new Vec3i(2, 2, 2))).stream())
                    .filter(pos -> !instanceData.cloud.containsKey(pos))
                    .map(pos -> context.transform.apply(pos, areaSize).add(lowerCoord))
                    .collect(Collectors.toCollection(HashSet::new));

            Set<BlockPos> remove = new HashSet<>();
            HashSet<BlockPos> start = new HashSet<>();

            // Do each one separately, since each block needs to be connected to floor separately
            check.forEach(checking ->
            {
                start.add(checking);
                if (visitRecursively(start, (changed, pos) ->
                {
                    IBlockState state = world.getBlockState(pos);
                    boolean isFoliage = RCBlockLogic.isFoliage(state, world, pos);
                    if (!RCBlockLogic.canStay(state, world, pos))
                        context.setBlock(pos, Blocks.AIR.getDefaultState(), 2);
                    else if (!isFoliage && !state.getBlock().isReplaceable(world, pos))
                        return false;
                    else if (isFoliage && remove.size() < MAX_TREE_SIZE && remove.add(pos))
                        neighbors(pos).forEach(changed::add);

                    return true;
                }))
                {
                    remove.forEach(pos -> context.setBlock(pos, Blocks.AIR.getDefaultState(), 2));
                }

                start.clear();
                remove.clear();
            });
        }
    }

    @Override
    public String getDisplayString()
    {
        return "Natural Air: " + sourceMatcher.getDisplayString(null);
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTNaturalAir(this, navigator, delegate);
    }

    @Override
    public InstanceData prepareInstanceData(StructurePrepareContext context, IvWorldData worldData)
    {
        return new InstanceData();
    }

    @Override
    public InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        InstanceData instanceData = new InstanceData();
        instanceData.readFromNBT(nbt);
        return instanceData;
    }

    @Override
    public double cloudExpansionDistance()
    {
        return naturalExpansionDistance;
    }

    @Override
    public double cloudExpansionRandomization()
    {
        return naturalExpansionRandomization;
    }

    @Override
    public boolean generatesInPhase(InstanceData instanceData, Phase phase)
    {
        return phase == Phase.BEFORE;
    }

    public static class InstanceData extends TransformerAbstractCloud.InstanceData
    {

    }

    public static class Serializer implements JsonDeserializer<TransformerNaturalAir>, JsonSerializer<TransformerNaturalAir>
    {
        private MCRegistry registry;

        public Serializer(MCRegistry registry)
        {
            this.registry = registry;
        }

        @Override
        public TransformerNaturalAir deserialize(JsonElement jsonElement, Type par2Type, JsonDeserializationContext context)
        {
            JsonObject jsonObject = JsonUtils.asJsonObject(jsonElement, "transformerNatural");

            String id = JsonUtils.getString(jsonObject, "id", null);

            String expression = TransformerReplace.Serializer.readLegacyMatcher(jsonObject, "source", "sourceMetadata"); // Legacy
            if (expression == null)
                expression = JsonUtils.getString(jsonObject, "sourceExpression", "");

            double naturalExpansionDistance = JsonUtils.getDouble(jsonObject, "naturalExpansionDistance", DEFAULT_NATURAL_EXPANSION_DISTANCE);
            double naturalExpansionRandomization = JsonUtils.getDouble(jsonObject, "naturalExpansionRandomization", DEFAULT_NATURAL_EXPANSION_RANDOMIZATION);

            TransformerNaturalAir transformer = new TransformerNaturalAir(id, expression, naturalExpansionDistance, naturalExpansionRandomization);

            transformer.destMatcher.setExpression(JsonUtils.getString(jsonObject, "destExpression", ""));

            return transformer;
        }

        @Override
        public JsonElement serialize(TransformerNaturalAir transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", transformer.id());
            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());
            jsonObject.addProperty("destExpression", transformer.destMatcher.getExpression());

            jsonObject.addProperty("naturalExpansionDistance", transformer.naturalExpansionDistance);
            jsonObject.addProperty("naturalExpansionRandomization", transformer.naturalExpansionRandomization);

            return jsonObject;
        }
    }
}
