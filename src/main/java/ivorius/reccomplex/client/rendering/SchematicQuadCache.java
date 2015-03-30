/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.client.rendering;

import com.google.common.base.Function;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.rendering.grid.GridQuadCache;
import ivorius.reccomplex.structures.schematics.SchematicFile;
import net.minecraft.block.Block;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;

/**
 * Created by lukas on 22.03.15.
 */
public class SchematicQuadCache
{
    public static GridQuadCache<?> createQuadCache(final SchematicFile schematic, float[] scale)
    {
        final Object handle = new Object();
        return GridQuadCache.createQuadCache(new int[]{schematic.width, schematic.height, schematic.length}, scale, new Function<Pair<BlockCoord, ForgeDirection>, Object>()
        {
            @Nullable
            @Override
            public Object apply(Pair<BlockCoord, ForgeDirection> input)
            {
                BlockCoord coord = input.getLeft();
                ForgeDirection direction = input.getRight();

                Block block = schematic.getBlock(coord);
                return block.isOpaqueCube() && schematic.shouldRenderSide(coord, direction)
                        ? handle
                        : null;
            }
        });
    }
}
