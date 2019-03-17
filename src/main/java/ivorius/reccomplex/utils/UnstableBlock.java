/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.block.state.IBlockState;

public interface UnstableBlock
{
    static boolean shouldSkipState(IBlockState state)
    {
        if (state.getBlock() instanceof UnstableBlock) {
            return ((UnstableBlock) state.getBlock()).shouldSkipOnGeneration(state);
        }

        return false;
    }

    default boolean shouldSkipOnGeneration(IBlockState state)
    {
        return true;
    }
}
