/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.client.rendering;

import net.minecraft.util.BlockPos;
import ivorius.ivtoolkit.rendering.grid.GridQuadCache;
import ivorius.reccomplex.structures.schematics.SchematicFile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

/**
 * Created by lukas on 22.03.15.
 */
public class SchematicQuadCache
{
    public static GridQuadCache<?> createQuadCache(final SchematicFile schematic, float[] scale)
    {
        final Object handle = new Object();
        return GridQuadCache.createQuadCache(new int[]{schematic.width, schematic.height, schematic.length}, scale, input -> {
            BlockPos coord = input.getLeft();
            EnumFacing direction = input.getRight();

            IBlockState blockState = schematic.getBlockState(coord);
            return blockState.getBlock().isOpaqueCube() && schematic.shouldRenderSide(coord, direction)
                    ? handle
                    : null;
        });
    }
}
