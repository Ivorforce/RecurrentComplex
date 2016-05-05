/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.client.rendering;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.rendering.grid.GridQuadCache;
import ivorius.reccomplex.structures.schematics.SchematicFile;
import ivorius.reccomplex.utils.BlockState;
import net.minecraft.block.Block;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by lukas on 22.03.15.
 */
public class SchematicQuadCache
{
    public static GridQuadCache<?> createQuadCache(final SchematicFile schematic, float[] scale)
    {
        final Object handle = new Object();
        return GridQuadCache.createQuadCache(new int[]{schematic.width, schematic.height, schematic.length}, scale, input -> {
            BlockCoord coord = input.getLeft();
            ForgeDirection direction = input.getRight();

            BlockState blockState = schematic.getBlockState(coord);
            return blockState.getBlock().isOpaqueCube() && schematic.shouldRenderSide(coord, direction)
                    ? handle
                    : null;
        });
    }
}
