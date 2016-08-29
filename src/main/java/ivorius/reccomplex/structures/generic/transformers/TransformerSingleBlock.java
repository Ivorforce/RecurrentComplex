/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.structures.StructureSpawnContext;
import net.minecraft.block.state.IBlockState;
import ivorius.reccomplex.utils.NBTStorable;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;

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
    public boolean skipGeneration(S instanceData, IBlockState state)
    {
        return matches(instanceData, state);
    }

    @Override
    public void transform(S instanceData, Phase phase, StructureSpawnContext context, IvWorldData worldData, List<Pair<Transformer, NBTStorable>> transformers)
    {
        IvBlockCollection blockCollection = worldData.blockCollection;
        int[] areaSize = new int[]{blockCollection.width, blockCollection.height, blockCollection.length};
        BlockPos lowerCoord = context.lowerCoord();

        for (BlockPos sourceCoord : blockCollection.area())
        {
            BlockPos worldCoord = context.transform.apply(sourceCoord, areaSize).add(lowerCoord);

            if (context.includes(worldCoord))
            {
                IBlockState state = blockCollection.getBlockState(sourceCoord);

                if (matches(instanceData, state))
                    transformBlock(instanceData, Phase.BEFORE, context, worldCoord, state);
            }
        }
    }

    public abstract boolean matches(S instanceData, IBlockState state);

    public abstract void transformBlock(S instanceData, Phase phase, StructureSpawnContext context, BlockPos coord, IBlockState sourceState);
}
