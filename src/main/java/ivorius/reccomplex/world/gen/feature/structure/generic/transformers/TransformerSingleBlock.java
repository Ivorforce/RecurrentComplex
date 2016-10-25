/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.transformers;

import ivorius.reccomplex.utils.*;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.world.gen.feature.structure.StructureSpawnContext;
import net.minecraft.block.state.IBlockState;

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
    public boolean skipGeneration(S instanceData, StructureSpawnContext context, BlockPos pos, IBlockState state, IvWorldData worldData, BlockPos sourcePos)
    {
        return matches(context.environment, instanceData, state);
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
            for (BlockPos sourceCoord : RCBlockAreas.mutablePositions(blockCollection.area()))
            {
                RCMutableBlockPos.add(RCAxisAlignedTransform.apply(sourceCoord, worldCoord, areaSize, context.transform), lowerCoord);

                if (context.includes(worldCoord))
                {
                    IBlockState state = blockCollection.getBlockState(sourceCoord);

                    if (matches(context.environment, instanceData, state))
                        transformBlock(instanceData, Phase.BEFORE, context, worldCoord, state);
                }
            }
        }
    }

    public abstract boolean generatesInPhase(S instanceData, Phase phase);

    public abstract boolean matches(Environment environment, S instanceData, IBlockState state);

    public abstract void transformBlock(S instanceData, Phase phase, StructureSpawnContext context, BlockPos coord, IBlockState sourceState);
}
