/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.block.state.IBlockState;

/**
 * Created by lukas on 06.05.16.
 */
public class BlockStates
{
    public static int toMetadata(IBlockState state)
    {
        return state.getBlock().getMetaFromState(state);
    }
}
