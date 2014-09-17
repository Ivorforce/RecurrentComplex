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
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class BlockTransformerPillar implements BlockTransformer
{
    public Block sourceBlock;
    public int sourceMetadata;

    public Block destBlock;
    public int destMetadata;

    public BlockTransformerPillar(Block sourceBlock, int sourceMetadata, Block destBlock, int destMetadata)
    {
        this.sourceBlock = sourceBlock;
        this.sourceMetadata = sourceMetadata;
        this.destBlock = destBlock;
        this.destMetadata = destMetadata;
    }

    @Override
    public boolean matches(Block block, int metadata)
    {
        return block == sourceBlock && (metadata < 0 || metadata == sourceMetadata);
    }

    @Override
    public void apply(World world, Random random, Phase phase, BlockCoord coord, Block sourceBlock, int sourceMetadata, IvWorldData worldData)
    {
        world.setBlock(coord.x, coord.y, coord.z, destBlock, destMetadata, 3);
        int y = coord.y;
        y--;

        while (y > 0)
        {
            Block block = world.getBlock(coord.x, y, coord.z);

            if (!(block.isReplaceable(world, coord.x, y, coord.z) || block.getMaterial() == Material.leaves || block.isFoliage(world, coord.x, y, coord.z)))
            {
                return;
            }

            world.setBlock(coord.x, y, coord.z, destBlock, destMetadata, 3);
            y--;
        }
    }

    @Override
    public void applyArea(World world, Random random, Phase phase, BlockArea area, AxisAlignedTransform2D transform2D, IvWorldData worldData)
    {

    }

    @Override
    public String displayString()
    {
        return "Pillar: " + sourceBlock.getLocalizedName() + "->" + destBlock.getLocalizedName();
    }

    @Override
    public boolean generatesInPhase(Phase phase)
    {
        return phase == Phase.BEFORE;
    }

    @Override
    public boolean generatesAreaInPhase(Phase phase)
    {
        return false;
    }
}
