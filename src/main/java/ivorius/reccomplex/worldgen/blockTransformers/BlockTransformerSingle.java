/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.blockTransformers;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.IvWorldData;
import net.minecraft.block.Block;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 17.09.14.
 */
public abstract class BlockTransformerSingle implements BlockTransformer
{
    @Override
    public boolean skipGeneration(Block block, int metadata)
    {
        return matches(block, metadata);
    }

    @Override
    public void transform(World world, Random random, Phase phase, BlockCoord origin, int[] size, AxisAlignedTransform2D transform, IvWorldData worldData, List<BlockTransformer> transformerList)
    {
        IvBlockCollection blockCollection = worldData.blockCollection;

        for (BlockCoord sourceCoord : blockCollection)
        {
            BlockCoord worldCoord = transform.apply(sourceCoord, size).add(origin);

            Block block = blockCollection.getBlock(sourceCoord);
            int meta = blockCollection.getMetadata(sourceCoord);

            if (matches(block, meta))
            {
                transformBlock(world, random, BlockTransformer.Phase.BEFORE, worldCoord, block, meta);
            }
        }
    }

    public abstract boolean matches(Block block, int metadata);

    public abstract void transformBlock(World world, Random random, Phase phase, BlockCoord coord, Block sourceBlock, int sourceMetadata);
}
