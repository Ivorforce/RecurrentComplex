/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.blockTransformers;

import ivorius.ivtoolkit.blocks.BlockArea;
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
    public boolean matches(Block block, int metadata)
    {
        return block == sourceBlock && (metadata < 0 || metadata == sourceMetadata);
    }

    @Override
    public void apply(World world, Random random, Phase phase, BlockCoord coord, Block sourceBlock, int sourceMetadata, IvWorldData worldData)
    {

    }

    @Override
    public void applyArea(World world, Random random, Phase phase, BlockArea area, AxisAlignedTransform2D transform2D, IvWorldData worldData)
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

    @Override
    public boolean generatesAreaInPhase(Phase phase)
    {
        return false;
    }
}
