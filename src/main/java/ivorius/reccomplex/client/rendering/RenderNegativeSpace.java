/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.client.rendering;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import ivorius.ivtoolkit.rendering.IvRenderHelper;
import ivorius.reccomplex.RCConfig;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

/**
 * Created by lukas on 06.08.14.
 */
public class RenderNegativeSpace implements ISimpleBlockRenderingHandler
{
    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
    {
        IvRenderHelper.renderCubeInvBlock(renderer, block, metadata);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
    {
        int metadata = world.getBlockMetadata(x, y, z);

        if (!RCConfig.hideRedundantNegativeSpace
                || (world.getBlock(x - 1, y, z) != block || world.getBlockMetadata(x - 1, y, z) != metadata)
                || (world.getBlock(x + 1, y, z) != block || world.getBlockMetadata(x + 1, y, z) != metadata)
                || (world.getBlock(x, y - 1, z) != block || world.getBlockMetadata(x, y - 1, z) != metadata)
                || (world.getBlock(x, y + 1, z) != block || world.getBlockMetadata(x, y + 1, z) != metadata)
                || (world.getBlock(x, y, z - 1) != block || world.getBlockMetadata(x, y, z - 1) != metadata)
                || (world.getBlock(x, y, z + 1) != block || world.getBlockMetadata(x, y, z + 1) != metadata))
        {
            return renderer.renderStandardBlock(block, x, y, z);
        }

        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId)
    {
        return true;
    }

    @Override
    public int getRenderId()
    {
        return RCBlockRendering.negativeSpaceRenderID;
    }
}
