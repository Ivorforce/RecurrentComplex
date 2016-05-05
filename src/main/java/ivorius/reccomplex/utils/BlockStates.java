/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.blocks.IvBlockCollection;
import net.minecraft.block.Block;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 06.05.16.
 */
public class BlockStates
{
    public static BlockState defaultState(@Nonnull  Block block)
    {
        return new BlockState(block, 0);
    }

    public static BlockState fromMetadata(@Nonnull  Block block, int metadata)
    {
        return new BlockState(block, metadata);
    }

    public static BlockState at(World world, BlockCoord coord)
    {
        return new BlockState(world.getBlock(coord.x, coord.y, coord.z), world.getBlockMetadata(coord.x, coord.y, coord.z));
    }

    public static BlockState at(IvBlockCollection collection, BlockCoord coord)
    {
        return new BlockState(collection.getBlock(coord), collection.getMetadata(coord));
    }

    public static int getMetadata(BlockState state)
    {
        return state.metadata;
    }
}
