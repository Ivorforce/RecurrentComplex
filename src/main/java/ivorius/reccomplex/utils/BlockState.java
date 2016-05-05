/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.block.Block;

import javax.annotation.Nonnull;

/**
 * Simulates 1.8's block state
 */
public class BlockState
{
    @Nonnull
    private final Block block;
    final int metadata;

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

    public BlockState with(int metadata)
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
