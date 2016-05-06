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
public interface IBlockState
{
    @Nonnull
    Block getBlock();

    @Nonnull
    IBlockState with(int metadata);
}
