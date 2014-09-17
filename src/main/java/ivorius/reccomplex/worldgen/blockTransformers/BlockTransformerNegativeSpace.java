/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.blockTransformers;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.IvWorldData;
import net.minecraft.block.Block;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class BlockTransformerNegativeSpace implements BlockTransformer
{
    public Block sourceBlock;
    public int sourceMetadata;

    public BlockTransformerNegativeSpace(Block sourceBlock, int sourceMetadata)
    {
        this.sourceBlock = sourceBlock;
        this.sourceMetadata = sourceMetadata;
    }

    @Override
    public boolean skipGeneration(Block block, int metadata)
    {
        return block == sourceBlock && (metadata < 0 || metadata == sourceMetadata);
    }

    @Override
    public void transform(World world, Random random, Phase phase, BlockCoord origin, int[] size, AxisAlignedTransform2D transform, IvWorldData worldData)
    {

    }

    @Override
    public String displayString()
    {
        return "Space: " + sourceBlock.getLocalizedName();
    }

    @Override
    public boolean generatesInPhase(Phase phase)
    {
        return false;
    }
}
