/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.blockTransformers;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.IvWorldData;
import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class BlockTransformerRuins implements BlockTransformer
{
    public float minDecay;
    public float maxDecay;
    public float decayChaos;

    public BlockTransformerRuins(float minDecay, float maxDecay, float decayChaos)
    {
        this.minDecay = minDecay;
        this.maxDecay = maxDecay;
        this.decayChaos = decayChaos;
    }

    @Override
    public boolean skipGeneration(Block block, int metadata)
    {
        return false;
    }

    @Override
    public void transform(World world, Random random, Phase phase, BlockCoord origin, int[] size, AxisAlignedTransform2D transform, IvWorldData worldData, List<BlockTransformer> transformerList)
    {
        IvBlockCollection blockCollection = worldData.blockCollection;

        float decayChaos = random.nextFloat() * this.decayChaos;
        if (this.maxDecay - this.minDecay > decayChaos)
            decayChaos = this.maxDecay - this.minDecay;

        float center = random.nextFloat() * (this.maxDecay - this.minDecay - decayChaos) + this.minDecay + decayChaos * 0.5f;

        BlockArea topArea = new BlockArea(new BlockCoord(0, blockCollection.height, 0), new BlockCoord(blockCollection.width, blockCollection.height, blockCollection.length));
        for (BlockCoord surfaceSourceCoord : topArea)
        {
            float decay = center + (random.nextFloat() - random.nextFloat()) * decayChaos * 0.5f;
            int removedBlocks = MathHelper.floor_float(decay * blockCollection.height + 0.5f);

            for (int ySource = 0; ySource < removedBlocks; ySource++)
            {
                BlockCoord sourceCoord = new BlockCoord(surfaceSourceCoord.x, blockCollection.height - 1 - ySource, surfaceSourceCoord.z);

                Block block = blockCollection.getBlock(sourceCoord);
                int meta = blockCollection.getMetadata(sourceCoord);

                boolean skip = false;
                for (BlockTransformer transformer : transformerList)
                {
                    if (transformer.skipGeneration(block, meta))
                    {
                        skip = true;
                        break;
                    }
                }

                if (!skip)
                {
                    BlockCoord worldCoord = transform.apply(sourceCoord, size).add(origin);
                    world.setBlockToAir(worldCoord.x, worldCoord.y, worldCoord.z);
                }
            }
        }
    }

    @Override
    public String displayString()
    {
        return "Ruins";
    }

    @Override
    public boolean generatesInPhase(Phase phase)
    {
        return phase == Phase.AFTER;
    }
}
