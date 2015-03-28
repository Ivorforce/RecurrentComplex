/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.client.rendering;

import com.google.common.base.Function;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.structures.StructureInfos;
import ivorius.reccomplex.utils.Directions;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;

/**
 * Created by lukas on 22.12.14.
 */
public class BlockQuadCache
{
    public static GridQuadCache<?> createQuadCache(final IvBlockCollection blockCollection, float[] scale)
    {
        final Object handle = new Object();

        final int[] size = {blockCollection.width, blockCollection.height, blockCollection.length};

        return GridQuadCache.createQuadCache(size, scale, new Function<Pair<BlockCoord, ForgeDirection>, Object>()
        {
            @Nullable
            @Override
            public Object apply(Pair<BlockCoord, ForgeDirection> input)
            {
                Block block = blockCollection.getBlock(input.getLeft());
                return block.isOpaqueCube() && blockCollection.shouldRenderSide(input.getLeft(), input.getRight())
                        ? handle
                        : null;
            }
        });
    }
}
