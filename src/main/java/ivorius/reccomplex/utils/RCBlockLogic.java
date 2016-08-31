/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by lukas on 31.08.16.
 */
public class RCBlockLogic
{
    public static boolean isFoliage(IBlockState state, World world, BlockPos pos)
    {
        Material material = state.getMaterial();
        return state.getBlock().isFoliage(world, pos) || state.getBlock().isWood(world, pos) || state.getBlock().isLeaves(state, world, pos) || material == Material.PLANTS;
    }

    public static boolean isTerrain(IBlockState block)
    {
        return block == Blocks.STONE
                || block == Blocks.DIRT || block == Blocks.GRASS
                || block == Blocks.SAND
                || block == Blocks.STAINED_HARDENED_CLAY || block == Blocks.HARDENED_CLAY || block == Blocks.CLAY
                || block == Blocks.COAL_ORE || block == Blocks.IRON_ORE // Common enough
                || block == Blocks.GRAVEL;
    }
}
