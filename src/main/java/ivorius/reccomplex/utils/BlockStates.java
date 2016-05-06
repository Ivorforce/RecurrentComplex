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
    public static IBlockState defaultState(@Nonnull  Block block)
    {
        return new BlockState(block, 0);
    }

    public static IBlockState fromMetadata(@Nonnull  Block block, int metadata)
    {
        return new BlockState(block, metadata);
    }

    public static IBlockState at(World world, BlockCoord coord)
    {
        return new BlockState(world.getBlock(coord.x, coord.y, coord.z), world.getBlockMetadata(coord.x, coord.y, coord.z));
    }

    public static IBlockState at(IvBlockCollection collection, BlockCoord coord)
    {
        return new BlockState(collection.getBlock(coord), collection.getMetadata(coord));
    }

    public static int getMetadata(IBlockState state)
    {
        return state instanceof BlockState ? ((BlockState) state).metadata : 0;
    }

    private static class BlockState implements IBlockState
    {
        @Nonnull
        private final Block block;
        private final int metadata;

        BlockState(@Nonnull Block block, int metadata)
        {
            this.block = block;
            this.metadata = metadata;
        }

        @Nonnull
        public Block getBlock()
        {
            return block;
        }

        public IBlockState with(int metadata)
        {
            return new BlockState(block, metadata);
        }

        @Override
        public String toString()
        {
            return "BlockState{" +
                    "block=" + block +
                    ", metadata=" + metadata +
                    '}';
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BlockState that = (BlockState) o;

            if (metadata != that.metadata) return false;
            return block != null ? block.equals(that.block) : that.block == null;

        }

        @Override
        public int hashCode()
        {
            int result = block != null ? block.hashCode() : 0;
            result = 31 * result + metadata;
            return result;
        }
    }
}
