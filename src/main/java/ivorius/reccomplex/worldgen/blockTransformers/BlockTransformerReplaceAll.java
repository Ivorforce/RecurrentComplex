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
 * Created by lukas on 25.05.14.
 */
public class BlockTransformerReplaceAll implements BlockTransformer
{
    public Block sourceBlock;
    public int sourceMetadata;

    public Block destBlock;
    public byte[] destMetadata;

    public BlockTransformerReplaceAll(Block sourceBlock, int sourceMetadata, Block destBlock, byte[] destMetadata)
    {
        this.sourceBlock = sourceBlock;
        this.sourceMetadata = sourceMetadata;
        this.destBlock = destBlock;
        this.destMetadata = destMetadata;
    }

    @Override
    public boolean skipGeneration(Block block, int metadata)
    {
        return block == sourceBlock && (metadata < 0 || metadata == sourceMetadata);
    }

    @Override
    public void transform(World world, Random random, Phase phase, BlockCoord origin, int[] size, AxisAlignedTransform2D transform, IvWorldData worldData, List<BlockTransformer> transformerList)
    {
        IvBlockCollection blockCollection = worldData.blockCollection;

        byte destMeta = destMetadata[random.nextInt(destMetadata.length)];

        for (BlockCoord sourceCoord : blockCollection)
        {
            BlockCoord worldCoord = transform.apply(sourceCoord, size).add(origin);

            Block block = blockCollection.getBlock(sourceCoord);
            int meta = blockCollection.getMetadata(sourceCoord);

            if (skipGeneration(block, meta))
            {
                world.setBlock(worldCoord.x, worldCoord.y, worldCoord.z, destBlock, destMeta, 3);
            }
        }
    }

    @Override
    public String displayString()
    {
        return "Replace All: " + sourceBlock.getLocalizedName() + "->" + destBlock.getLocalizedName();
    }

    @Override
    public boolean generatesInPhase(Phase phase)
    {
        return phase == Phase.BEFORE;
    }
}
