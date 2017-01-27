/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.transformers;

import com.google.common.collect.Sets;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.blocks.IvMutableBlockPos;
import ivorius.ivtoolkit.random.BlurredValueField;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.ivtoolkit.world.chunk.gen.StructureBoundingBoxes;
import ivorius.reccomplex.utils.*;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLiveContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiPredicate;

/**
 * Created by lukas on 09.09.16.
 */
public abstract class TransformerAbstractCloud<S extends TransformerAbstractCloud.InstanceData> extends Transformer<S>
{
    public TransformerAbstractCloud(@Nonnull String id)
    {
        super(id);
    }

    public static <T> boolean visitRecursively(HashSet<T> start, BiPredicate<Set<T>, T> changedConsumer)
    {
        Set<T> changedL = start;
        Set<T> changedR = new HashSet<>();
        while (changedL.size() > 0 || changedR.size() > 0)
        {
            Set<T> current = changedL.size() > 0 ? changedL : changedR;
            final Set<T> next = current == changedL ? changedR : changedL;
            for (T t : current)
            {
                if (!changedConsumer.test(next, t))
                    return false;
            }
            current.clear();
        }

        return true;
    }

    public abstract double cloudExpansionDistance();

    public abstract double cloudExpansionRandomization();

    @Override
    public boolean skipGeneration(S instanceData, StructureLiveContext context, BlockPos pos, IBlockState state, IvWorldData worldData, BlockPos sourcePos)
    {
        return matches(instanceData, state);
    }

    public TObjectDoubleMap<BlockPos> buildCloud(S instanceData, IvWorldData worldData, StructurePrepareContext context, TransformerMulti transformer, TransformerMulti.InstanceData transformerInstanceData)
    {
        Random random = context.random;
        Environment environment = context.environment;
        BlockPos lowerCoord = StructureBoundingBoxes.min(context.boundingBox);
        int[] strucSize = new int[]{worldData.blockCollection.width, worldData.blockCollection.height, worldData.blockCollection.length};

        TObjectDoubleMap<BlockPos> cloud = new TObjectDoubleHashMap<>();
        ivorius.ivtoolkit.random.BlurredValueField blurredValueField = new BlurredValueField(strucSize);

        int gridCoords = 1;
        for (int d : strucSize) gridCoords *= d;
        int values = MathHelper.floor_float(gridCoords * (1f / 25f) + 0.5f);

        for (int i = 0; i < values; i++)
            blurredValueField.addValue(1 + (random.nextFloat() - random.nextFloat()) * (float) cloudExpansionRandomization() / 100f, random);

        BlockAreas.mutablePositions(worldData.blockCollection.area()).forEach(pos ->
        {
            IBlockState state = worldData.blockCollection.getBlockState(pos);
            BlockPos worldCoord = context.transform.apply(pos, strucSize).add(lowerCoord);
            if (matches(instanceData, state) && canPenetrate(environment, worldData, worldCoord, 1, transformer, transformerInstanceData))
                cloud.put(pos.toImmutable(), 1);
        });

        double expansionDistance = cloudExpansionDistance();
        BlockPos.MutableBlockPos sidePos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos sideWorldCoord = new BlockPos.MutableBlockPos();

        if (expansionDistance > 0.000001)
        {
            // The code below will be called _often_, so let's cache the divisions
            double[] sideFalloffs = new double[6];
            List<EnumFacing> checkSides = new ArrayList<>();
            for (EnumFacing side : EnumFacing.values())
            {
                double sideExpansionDistance = cloudExpansionDistance(side);
                double sideFalloff = sideExpansionDistance > 0.000001 ? 1.0f / sideExpansionDistance / expansionDistance : 0;
                if (sideFalloff > 0)
                {
                    checkSides.add(side);
                    sideFalloffs[side.getIndex()] = sideFalloff;
                }
            }

            visitRecursively(Sets.newHashSet(cloud.keySet()), (changed, pos) ->
            {
                double density = cloud.get(pos);

                for (EnumFacing side : checkSides)
                {
                    double sideFalloff = sideFalloffs[side.getIndex()];

                    IvMutableBlockPos.offset(pos, sidePos, side);

                    double sideDensity = density - sideFalloff * blurredValueField.getValue(sidePos.getX(), sidePos.getY(), sidePos.getZ());
                    if (sideDensity <= 0 || cloud.get(sidePos) >= sideDensity - 0.00001)
                        continue;

                    IvMutableBlockPos.add(RCAxisAlignedTransform.apply(sidePos, sideWorldCoord, strucSize, context.transform), lowerCoord);
                    if (!canPenetrate(environment, worldData, sideWorldCoord, sideDensity, transformer, transformerInstanceData))
                        continue;

                    BlockPos immutableSidePos = sidePos.toImmutable();

                    cloud.put(immutableSidePos, sideDensity);
                    changed.add(immutableSidePos);
                }
                return true;
            });
        }

        return cloud;
    }

    public boolean canPenetrate(Environment environment, IvWorldData worldData, BlockPos pos, double density, TransformerMulti transformer, TransformerMulti.InstanceData transformerID)
    {
        return true;
    }

    protected double cloudExpansionDistance(EnumFacing side)
    {
        return 1;
    }

    @Override
    public void configureInstanceData(S s, StructurePrepareContext context, IvWorldData worldData, RunTransformer transformer)
    {
        s.cloud = buildCloud(s, worldData, context, transformer.transformer, transformer.instanceData);
    }

    @Override
    public void transform(S instanceData, Phase phase, StructureSpawnContext context, IvWorldData worldData, RunTransformer transformer)
    {
        if (generatesInPhase(instanceData, phase))
        {
            IvBlockCollection blockCollection = worldData.blockCollection;
            int[] areaSize = new int[]{blockCollection.width, blockCollection.height, blockCollection.length};
            BlockPos lowerCoord = StructureBoundingBoxes.min(context.boundingBox);

            BlockPos.MutableBlockPos worldCoord = new BlockPos.MutableBlockPos();
            instanceData.cloud.forEachEntry((sourcePos, density) ->
            {
                IvMutableBlockPos.add(RCAxisAlignedTransform.apply(sourcePos, worldCoord, areaSize, context.transform), lowerCoord);
                transformBlock(instanceData, phase, context, sourcePos, worldCoord, worldData.blockCollection.getBlockState(sourcePos), density);
                return true;
            });
        }
    }

    public abstract boolean generatesInPhase(S instanceData, Transformer.Phase phase);

    public abstract boolean matches(S instanceData, IBlockState state);

    public abstract void transformBlock(S instanceData, Phase phase, StructureSpawnContext context, BlockPos sourcePos, BlockPos pos, IBlockState sourceState, double density);

    public static class InstanceData implements NBTStorable
    {
        public TObjectDoubleMap<BlockPos> cloud = new TObjectDoubleHashMap<>();

        public void readFromNBT(NBTBase base)
        {
            NBTTagCompound compound = base instanceof NBTTagCompound ? (NBTTagCompound) base : new NBTTagCompound();
            NBTTagLists.compoundsFrom(compound, "cloud").forEach(cloudCompound ->
            {
                BlockPos pos = BlockPositions.readFromNBT("particle", cloudCompound);
                if (pos != null)
                    cloud.put(pos, cloudCompound.getDouble("density"));
            });
        }

        @Override
        public NBTBase writeToNBT()
        {
            NBTTagCompound compound = new NBTTagCompound();

            List<NBTTagCompound> cloudCompounds = new ArrayList<>();
            cloud.forEachEntry((pos, density) ->
            {
                NBTTagCompound cloudCompound = new NBTTagCompound();
                BlockPositions.writeToNBT("particle", pos, cloudCompound);
                cloudCompound.setDouble("density", density);
                cloudCompounds.add(cloudCompound);
                return true;
            });
            NBTTagLists.writeTo(compound, "cloud", cloudCompounds);

            return compound;
        }
    }
}
