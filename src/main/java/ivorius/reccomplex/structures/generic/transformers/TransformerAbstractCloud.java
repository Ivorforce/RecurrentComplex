/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.reccomplex.random.BlurredValueField;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Created by lukas on 09.09.16.
 */
public abstract class TransformerAbstractCloud<S extends TransformerAbstractCloud.InstanceData> extends Transformer<S>
{
    public TransformerAbstractCloud(@Nonnull String id)
    {
        super(id);
    }

    public abstract double naturalExpansionDistance();

    public abstract double naturalExpansionRandomization();

    @Override
    public boolean skipGeneration(StructureSpawnContext context, S instanceData, IBlockState state)
    {
        return matches(instanceData, state);
    }

    public TObjectDoubleMap<BlockPos> buildCloud(S instanceData, Random random, IvWorldData worldData)
    {
        TObjectDoubleMap<BlockPos> cloud = new TObjectDoubleHashMap<>();
        int[] blurredFieldSize = new int[]{worldData.blockCollection.width, worldData.blockCollection.height, worldData.blockCollection.length};
        BlurredValueField blurredValueField = new BlurredValueField(blurredFieldSize);

        int gridCoords = 1;
        for (int d : blurredFieldSize) gridCoords *= d;
        int values = MathHelper.floor_float(gridCoords * (1f / 25f) + 0.5f);

        for (int i = 0; i < values; i++)
            blurredValueField.addValue(1 + (random.nextFloat() - random.nextFloat()) * (float) naturalExpansionRandomization() / 100f, random);

        worldData.blockCollection.area().stream().forEach(pos ->
        {
            IBlockState state = worldData.blockCollection.getBlockState(pos);
            if (matches(instanceData, state))
                cloud.put(pos, 1);
        });

        final double falloff = 1.0 / naturalExpansionDistance();

        Set<BlockPos> changedL = new HashSet<>();
        changedL.addAll(cloud.keySet());
        Set<BlockPos> changedR = new HashSet<>();
        while (changedL.size() > 0 || changedR.size() > 0)
        {
            Set<BlockPos> prev = changedL.size() > 0 ? changedL : changedR;
            final Set<BlockPos> changed = prev == changedL ? changedR : changedL;
            prev.forEach(pos ->
            {
                for (EnumFacing side : EnumFacing.values())
                {
                    double modifier = naturalExpansionDistance(side);
                    if (modifier > 0.000001)
                    {
                        BlockPos sidePos = pos.offset(side);
                        double sideDensity = cloud.get(pos) - (falloff * (1.0f / modifier) * blurredValueField.getValue(sidePos.getX(), sidePos.getY(), sidePos.getZ()));

                        if (sideDensity > 0 && cloud.get(sidePos) < sideDensity)
                        {
                            cloud.put(sidePos, sideDensity);
                            changed.add(sidePos);
                        }
                    }
                }
            });
            prev.clear();
        }

        return cloud;
    }

    protected double naturalExpansionDistance(EnumFacing side)
    {
        return 1;
    }

    @Override
    public S prepareInstanceData(StructurePrepareContext context, IvWorldData worldData)
    {
        S s = prepareInstanceDataCloud(context, worldData);
        s.cloud = buildCloud(s, context.random, worldData);
        return s;
    }

    public abstract S prepareInstanceDataCloud(StructurePrepareContext context, IvWorldData worldData);

    @Override
    public void transform(S instanceData, Transformer.Phase phase, StructureSpawnContext context, IvWorldData worldData, List<Pair<Transformer, NBTStorable>> transformers)
    {
        if (generatesInPhase(instanceData, phase))
        {
            IvBlockCollection blockCollection = worldData.blockCollection;
            int[] areaSize = new int[]{blockCollection.width, blockCollection.height, blockCollection.length};
            BlockPos lowerCoord = context.lowerCoord();

            instanceData.cloud.forEachEntry((pos, density) ->
            {
                BlockPos worldCoord = context.transform.apply(pos, areaSize).add(lowerCoord);
                transformBlock(instanceData, phase, context, pos, worldCoord, worldData.blockCollection.getBlockState(pos), density);
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
            NBTTagLists.compoundsFrom(compound, "cloud").stream().forEach(cloudCompound -> {
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
            cloud.forEachEntry((pos, density) -> {
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
