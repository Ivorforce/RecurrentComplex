/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.transformers;

import com.google.gson.*;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.MCRegistry;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.block.RCBlocks;
import ivorius.reccomplex.gui.editstructure.transformers.TableDataSourceBTNatural;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.json.JsonUtils;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.utils.expression.BlockMatcher;
import ivorius.reccomplex.utils.RCBlockLogic;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public class TransformerNatural extends TransformerAbstractCloud<TransformerNatural.InstanceData>
{
    public static final double DEFAULT_NATURAL_EXPANSION_DISTANCE = 4.0;
    public static final double DEFAULT_NATURAL_EXPANSION_RANDOMIZATION = 6.0;

    public BlockMatcher sourceMatcher;

    public double naturalExpansionDistance;
    public double naturalExpansionRandomization;

    public TransformerNatural()
    {
        this(null, BlockMatcher.of(RecurrentComplex.specialRegistry, RCBlocks.genericSolid, 0), DEFAULT_NATURAL_EXPANSION_DISTANCE, DEFAULT_NATURAL_EXPANSION_RANDOMIZATION);
    }

    public TransformerNatural(@Nullable String id, String sourceMatcherExpression, double naturalExpansionDistance, double naturalExpansionRandomization)
    {
        super(id != null ? id : randomID(TransformerNatural.class));
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
    public boolean matches(InstanceData instanceData, IBlockState state)
    {
        return sourceMatcher.test(state);
    }

    @Override
    protected double cloudExpansionDistance(EnumFacing side)
    {
        return side == EnumFacing.UP ? 0
                : side.getHorizontalIndex() >= 0 ? 0.5f
                : super.cloudExpansionDistance(side);
    }

    @Override
    public boolean canPenetrate(Environment environment, IvWorldData worldData, BlockPos pos, double density, TransformerMulti transformer, TransformerMulti.InstanceData transformerID)
    {
        IBlockState state = environment.world.getBlockState(pos);
        return density >= 1 || state.getBlock().isReplaceable(environment.world, pos)
                || RCBlockLogic.isFoliage(state, environment.world, pos);
    }

    @Override
    public void transformBlock(InstanceData instanceData, Phase phase, StructureSpawnContext context, BlockPos sourcePos, BlockPos pos, IBlockState sourceState, double density)
    {
        World world = context.environment.world;
        Biome biome = context.environment.biome;
        IBlockState topBlock = biome.topBlock != null ? biome.topBlock : Blocks.AIR.getDefaultState();
        IBlockState fillerBlock = biome.fillerBlock != null ? biome.fillerBlock : Blocks.AIR.getDefaultState();
        IBlockState mainBlock = Blocks.STONE.getDefaultState();

        boolean useStoneBlock = pos.getY() < world.getSeaLevel();
        IBlockState setBlock = useStoneBlock ? mainBlock : (instanceData.cloud.containsKey(sourcePos.up()) ? fillerBlock : topBlock);

        if (world.provider.getDimension() == -1)
            setBlock = Blocks.NETHERRACK.getDefaultState();
        else if (world.provider.getDimension() == 1)
            setBlock = Blocks.END_STONE.getDefaultState();

        context.setBlock(pos, setBlock, 2);
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
    public String getDisplayString()
    {
        return "Natural: " + sourceMatcher.getDisplayString(null);
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate)
    {
        return new TableDataSourceBTNatural(this, navigator, delegate);
    }

    public static class InstanceData extends TransformerAbstractCloud.InstanceData
    {

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
            JsonObject jsonObject = JsonUtils.asJsonObject(jsonElement, "transformerNatural");

            String id = JsonUtils.getString(jsonObject, "id", null);

            String expression = TransformerReplace.Serializer.readLegacyMatcher(jsonObject, "source", "sourceMetadata"); // Legacy
            if (expression == null)
                expression = JsonUtils.getString(jsonObject, "sourceExpression", "");

            double naturalExpansionDistance = JsonUtils.getDouble(jsonObject, "naturalExpansionDistance", DEFAULT_NATURAL_EXPANSION_DISTANCE);
            double naturalExpansionRandomization = JsonUtils.getDouble(jsonObject, "naturalExpansionRandomization", DEFAULT_NATURAL_EXPANSION_RANDOMIZATION);

            return new TransformerNatural(id, expression, naturalExpansionDistance, naturalExpansionRandomization);
        }

        @Override
        public JsonElement serialize(TransformerNatural transformer, Type par2Type, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("id", transformer.id());
            jsonObject.addProperty("sourceExpression", transformer.sourceMatcher.getExpression());

            jsonObject.addProperty("naturalExpansionDistance", transformer.naturalExpansionDistance);
            jsonObject.addProperty("naturalExpansionRandomization", transformer.naturalExpansionRandomization);

            return jsonObject;
        }
    }
}
