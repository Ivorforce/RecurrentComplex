/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import com.google.common.collect.Sets;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.reccomplex.random.BlurredValueField;
import ivorius.reccomplex.structures.Environment;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.utils.NBTStorable;
import ivorius.reccomplex.utils.RCAxisAlignedTransform;
import ivorius.reccomplex.utils.RCBlockAreas;
import ivorius.reccomplex.utils.RCMutableBlockPos;
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
        Set<T> changedR = new HashSet<>();
        while (start.size() > 0 || changedR.size() > 0)
        {
            Set<T> prev = start.size() > 0 ? start : changedR;
            final Set<T> changed = prev == start ? changedR : start;
            for (T t : prev)
            {
                if (!changedConsumer.test(changed, t))
                    return false;
            }
            prev.clear();
        }

        return true;
    }

    public abstract double naturalExpansionDistance();

    public abstract double naturalExpansionRandomization();

    @Override
    public boolean skipGeneration(S instanceData, Environment environment, BlockPos pos, IBlockState state)
    {
        return matches(instanceData, state);
    }

    public TObjectDoubleMap<BlockPos> buildCloud(S instanceData, IvWorldData worldData, StructurePrepareContext context, TransformerMulti transformer, TransformerMulti.InstanceData transformerInstanceData)
    {
        Random random = context.random;
        Environment environment = context.environment;
        BlockPos lowerCoord = context.lowerCoord();
        int[] strucSize = new int[]{worldData.blockCollection.width, worldData.blockCollection.height, worldData.blockCollection.length};

        TObjectDoubleMap<BlockPos> cloud = new TObjectDoubleHashMap<>();
        BlurredValueField blurredValueField = new BlurredValueField(strucSize);

        int gridCoords = 1;
        for (int d : strucSize) gridCoords *= d;
        int values = MathHelper.floor_float(gridCoords * (1f / 25f) + 0.5f);

        for (int i = 0; i < values; i++)
            blurredValueField.addValue(1 + (random.nextFloat() - random.nextFloat()) * (float) naturalExpansionRandomization() / 100f, random);

        RCBlockAreas.mutablePositions(worldData.blockCollection.area()).forEach(pos ->
        {
            IBlockState state = worldData.blockCollection.getBlockState(pos);
            BlockPos worldCoord = context.transform.apply(pos, strucSize).add(lowerCoord);
            if (matches(instanceData, state) && canPenetrate(environment, worldData, worldCoord, 1, transformer, transformerInstanceData))
                cloud.put(pos.toImmutable(), 1);
        });

        double naturalExpansionDistance = naturalExpansionDistance();
        BlockPos.MutableBlockPos sidePos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos sideWorldCoord = new BlockPos.MutableBlockPos();
        if (naturalExpansionDistance > 0.000001)
        {
            final double falloff = 1.0 / naturalExpansionDistance;

            visitRecursively(Sets.newHashSet(cloud.keySet()), (changed, pos) ->
            {
                double density = cloud.get(pos);

                for (EnumFacing side : EnumFacing.values())
                {
                    double modifier = naturalExpansionDistance(side);
                    if (modifier > 0.000001)
                    {
                        RCMutableBlockPos.offset(pos, sidePos, side);
                        RCMutableBlockPos.add(RCAxisAlignedTransform.apply(sidePos, sideWorldCoord, strucSize, context.transform), lowerCoord);

                        double sideDensity = density - (falloff * (1.0 / modifier) * blurredValueField.getValue(sidePos.getX(), sidePos.getY(), sidePos.getZ()));

                        if (sideDensity > 0 && cloud.get(sidePos) < sideDensity && canPenetrate(environment, worldData, sideWorldCoord, sideDensity, transformer, transformerInstanceData))
                        {
                            BlockPos immutableSidePos = sidePos.toImmutable();

                            cloud.put(immutableSidePos, sideDensity);
                            changed.add(immutableSidePos);
                        }
                    }
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

    protected double naturalExpansionDistance(EnumFacing side)
    {
        return 1;
    }

    @Override
    public void configureInstanceData(S s, StructurePrepareContext context, IvWorldData worldData, TransformerMulti transformer, TransformerMulti.InstanceData transformerID)
    {
        s.cloud = buildCloud(s, worldData, context, transformer, transformerID);
    }

    @Override
    public boolean mayGenerate(S instanceData, StructureSpawnContext context, IvWorldData worldData, TransformerMulti transformer, TransformerMulti.InstanceData transformerID)
    {
        return true;
    }

    @Override
    public void transform(S instanceData, Transformer.Phase phase, StructureSpawnContext context, IvWorldData worldData, TransformerMulti transformer, TransformerMulti.InstanceData transformerID)
    {
        if (generatesInPhase(instanceData, phase))
        {
            IvBlockCollection blockCollection = worldData.blockCollection;
            int[] areaSize = new int[]{blockCollection.width, blockCollection.height, blockCollection.length};
            BlockPos lowerCoord = context.lowerCoord();

            BlockPos.MutableBlockPos worldCoord = new BlockPos.MutableBlockPos();
            instanceData.cloud.forEachEntry((sourcePos, density) ->
            {
                RCMutableBlockPos.add(RCAxisAlignedTransform.apply(sourcePos, worldCoord, areaSize, context.transform), lowerCoord);
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
                return true;
            });
            NBTTagLists.writeTo(compound, "cloud", cloudCompounds);

            return compound;
        }
    }
}
