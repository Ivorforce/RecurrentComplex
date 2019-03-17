/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.transformers;

import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.ivtoolkit.blocks.IvMutableBlockPos;
import ivorius.ivtoolkit.world.chunk.gen.StructureBoundingBoxes;
import ivorius.reccomplex.nbt.NBTStorable;
import ivorius.reccomplex.utils.*;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLiveContext;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 17.09.14.
 */
public abstract class TransformerSingleBlock<S extends NBTStorable> extends Transformer<S>
{
    public TransformerSingleBlock(@Nonnull String id)
    {
        super(id);
    }

    @Override
    public boolean skipGeneration(S instanceData, StructureLiveContext context, BlockPos pos, IBlockState state, IvWorldData worldData, BlockPos sourcePos)
    {
        return matches(context.environment, instanceData, sourcePos, state);
    }

    @Override
    public void transform(S instanceData, Phase phase, StructureSpawnContext context, IvWorldData worldData, RunTransformer transformer)
    {
        if (!generatesInPhase(instanceData, phase))
            return;

        IvBlockCollection blockCollection = worldData.blockCollection;

        StructureBoundingBox relevantSourceArea = context.sourceIntersection(BlockAreas.toBoundingBox(blockCollection.area()));
        if (relevantSourceArea == null)
            return;

        BlockPos lowerCoord = StructureBoundingBoxes.min(context.boundingBox);
        int[] areaSize = new int[]{blockCollection.width, blockCollection.height, blockCollection.length};

        // Freeze height to speed up light calculation
        context.freezeHeightMap(context.intersection(context.boundingBox));

        BlockPos.MutableBlockPos worldCoord = new BlockPos.MutableBlockPos();
        for (BlockPos sourceCoord : RCStructureBoundingBoxes.mutablePositions(relevantSourceArea))
        {
            IvMutableBlockPos.add(context.transform.applyOn(sourceCoord, worldCoord, areaSize), lowerCoord);

            if (!context.includesComplex(worldCoord))
                continue;

            IBlockState state = blockCollection.getBlockState(sourceCoord);

            if (matches(context.environment, instanceData, sourceCoord, state))
                transformBlock(instanceData, Phase.BEFORE, context, transformer, areaSize, worldCoord, state);
        }

        context.meltHeightMap();
    }

    public abstract boolean generatesInPhase(S instanceData, Phase phase);

    public abstract boolean matches(Environment environment, S instanceData, BlockPos sourcePos, IBlockState state);

    public abstract void transformBlock(S instanceData, Phase phase, StructureSpawnContext context, RunTransformer transformer, int[] areaSize, BlockPos coord, IBlockState sourceState);
}
