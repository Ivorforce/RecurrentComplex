/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.blocktransformers;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.structures.StructureSpawnContext;
import net.minecraft.block.Block;

import java.util.List;

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
    public void transform(Phase phase, StructureSpawnContext context, IvWorldData worldData, List<BlockTransformer> transformerList)
    {
        IvBlockCollection blockCollection = worldData.blockCollection;

        byte destMeta = destMetadata[context.random.nextInt(destMetadata.length)];

        for (BlockCoord sourceCoord : blockCollection)
        {
            BlockCoord worldCoord = context.transform.apply(sourceCoord, context.boundingBoxSize()).add(context.lowerCoord());

            Block block = blockCollection.getBlock(sourceCoord);
            int meta = blockCollection.getMetadata(sourceCoord);

            if (skipGeneration(block, meta))
            {
                context.world.setBlock(worldCoord.x, worldCoord.y, worldCoord.z, destBlock, destMeta, 3);
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
