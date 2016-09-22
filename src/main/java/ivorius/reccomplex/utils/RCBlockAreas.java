/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockAreas;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by lukas on 16.09.16.
 */
public class RCBlockAreas
{
    @Nonnull
    public static BlockArea expand(BlockArea area, EnumFacing side, int amount)
    {
        switch (side)
        {
            case UP:
                return BlockAreas.expand(area, BlockPos.ORIGIN, new BlockPos(0, amount, 0));
            case DOWN:
                return BlockAreas.expand(area, new BlockPos(0, amount, 0), BlockPos.ORIGIN);
            case NORTH:
                return BlockAreas.expand(area, new BlockPos(0, 0, amount), BlockPos.ORIGIN);
            case EAST:
                return BlockAreas.expand(area, BlockPos.ORIGIN, new BlockPos(amount, 0, 0));
            case SOUTH:
                return BlockAreas.expand(area, BlockPos.ORIGIN, new BlockPos(0, 0, amount));
            case WEST:
                return BlockAreas.expand(area, new BlockPos(amount, 0, 0), BlockPos.ORIGIN);
            default:
                throw new IllegalArgumentException();
        }
    }

    public static Iterable<BlockPos> positions(BlockArea area)
    {
        return BlockPos.getAllInBox(area.getPoint1(), area.getPoint2());
    }

    public static Iterable<BlockPos.MutableBlockPos> mutablePositions(BlockArea area)
    {
        return BlockPos.getAllInBoxMutable(area.getPoint1(), area.getPoint2());
    }

    public static Stream<BlockPos> streamPositions(BlockArea area)
    {
        return StreamSupport.stream(BlockPos.getAllInBox(area.getPoint1(), area.getPoint2()).spliterator(), false);
    }

    public static Stream<BlockPos.MutableBlockPos> streamMutablePositions(BlockArea area)
    {
        return StreamSupport.stream(BlockPos.getAllInBoxMutable(area.getPoint1(), area.getPoint2()).spliterator(), false);
    }
}
