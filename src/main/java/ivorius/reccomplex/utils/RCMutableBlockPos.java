/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * Created by lukas on 22.09.16.
 */
public class RCMutableBlockPos
{
    public static BlockPos.MutableBlockPos add(BlockPos.MutableBlockPos pos, BlockPos add)
    {
        return pos.setPos(pos.getX() + add.getX(), pos.getY() + add.getY(), pos.getZ() + add.getZ());
    }

    public static BlockPos.MutableBlockPos offset(BlockPos pos, BlockPos.MutableBlockPos dest, EnumFacing facing)
    {
        return offset(pos, dest, facing, 1);
    }

    public static BlockPos.MutableBlockPos offset(BlockPos pos, BlockPos.MutableBlockPos dest, EnumFacing facing, int amount)
    {
        return dest.setPos(pos.getX() + facing.getFrontOffsetX() * amount, pos.getY() + facing.getFrontOffsetY() * amount, pos.getZ() + facing.getFrontOffsetZ() * amount);
    }
}
