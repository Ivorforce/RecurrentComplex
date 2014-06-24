/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.worldgen.blockTransformers;

import ivorius.structuregen.ivtoolkit.tools.IvWorldData;
import net.minecraft.block.Block;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class BlockTransformerReplace implements BlockTransformer
{
    public Block sourceBlock;
    public int sourceMetadata;

    public Block destBlock;
    public byte[] destMetadata;

    public BlockTransformerReplace(Block sourceBlock, int sourceMetadata, Block destBlock, byte[] destMetadata)
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
    public void apply(World world, Random random, int x, int y, int z, Block sourceBlock, int sourceMetadata, IvWorldData worldData)
    {
        world.setBlock(x, y, z, destBlock, destMetadata[random.nextInt(destMetadata.length)], 3);
    }

    @Override
    public String displayString()
    {
        return "Replace: " + sourceBlock.getLocalizedName() + "->" + destBlock.getLocalizedName();
    }

    @Override
    public boolean generatesBefore()
    {
        return false;
    }
}
