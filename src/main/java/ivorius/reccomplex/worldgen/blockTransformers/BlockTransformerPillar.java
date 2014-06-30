/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.blockTransformers;

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
    public void apply(World world, Random random, boolean beforeGeneration, int x, int y, int z, Block sourceBlock, int sourceMetadata, IvWorldData worldData)
    {
        world.setBlock(x, y, z, destBlock, destMetadata, 3);
        y--;

        while (y > 0)
        {
            Block block = world.getBlock(x, y, z);

            if (!(block.isReplaceable(world, x, y, z) || block.getMaterial() == Material.leaves || block.isFoliage(world, x, y, z)))
            {
                return;
            }

            world.setBlock(x, y, z, destBlock, destMetadata, 3);
            y--;
        }
    }

    @Override
    public String displayString()
    {
        return "Pillar: " + sourceBlock.getLocalizedName() + "->" + destBlock.getLocalizedName();
    }

    @Override
    public boolean generatesBefore()
    {
        return true;
    }

    @Override
    public boolean generatesAfter()
    {
        return false;
    }
}
