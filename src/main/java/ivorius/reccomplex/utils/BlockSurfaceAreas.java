/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.gui.IntegerRange;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by lukas on 14.09.16.
 */
public class BlockSurfaceAreas
{
    public static int sideLength(BlockSurfaceArea area, EnumFacing side)
    {
        int[] size = area.areaSize();
        switch (side)
        {
            case EAST:
            case WEST:
                return size[0];
            case UP:
            case DOWN:
                return size[1];
            case NORTH:
            case SOUTH:
                return size[2];
        }

        throw new IllegalArgumentException();
    }

    public static BlockSurfaceArea side(BlockSurfaceArea area, EnumFacing side)
    {
        BlockSurfacePos lowerCorner = area.getLowerCorner();
        BlockSurfacePos higherCorner = area.getHigherCorner();

        switch (side)
        {
            case NORTH:
                return new BlockSurfaceArea(lowerCorner, new BlockSurfacePos(higherCorner.getX(), lowerCorner.getZ()));
            case EAST:
                return new BlockSurfaceArea(new BlockSurfacePos(higherCorner.getX(), lowerCorner.getZ()), higherCorner);
            case SOUTH:
                return new BlockSurfaceArea(new BlockSurfacePos(lowerCorner.getX(), higherCorner.getZ()), higherCorner);
            case WEST:
                return new BlockSurfaceArea(lowerCorner, new BlockSurfacePos(lowerCorner.getX(), higherCorner.getZ()));
            default:
                throw new IllegalArgumentException();
        }
    }

    @Nullable
    public static BlockSurfaceArea shrink(BlockSurfaceArea area, EnumFacing side, int amount)
    {
        switch (side)
        {
            case NORTH:
                return shrink(area, new BlockSurfacePos(0, amount), BlockSurfacePos.ORIGIN);
            case EAST:
                return shrink(area, BlockSurfacePos.ORIGIN, new BlockSurfacePos(amount, 0));
            case SOUTH:
                return shrink(area, BlockSurfacePos.ORIGIN, new BlockSurfacePos(0, amount));
            case WEST:
                return shrink(area, new BlockSurfacePos(amount, 0), BlockSurfacePos.ORIGIN);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Nullable
    public static BlockSurfaceArea shrink(BlockSurfaceArea area, BlockSurfacePos lower, BlockSurfacePos higher)
    {
        BlockSurfacePos p1 = area.getPoint1();
        BlockSurfacePos p2 = area.getPoint2();
        IntegerRange x = shrink(p1.getX(), p2.getX(), lower.getX(), higher.getX());
        IntegerRange z = shrink(p1.getZ(), p2.getZ(), lower.getZ(), higher.getZ());

        return x != null && z != null
                ? new BlockSurfaceArea(new BlockSurfacePos(x.min, z.min), new BlockSurfacePos(x.max, z.max))
                : null;
    }

    @Nullable
    private static IntegerRange shrink(int l, int r, int shrMin, int shrMax)
    {
        boolean c = l < r;
        return Math.abs(l - r) >= shrMin + shrMax
                ? new IntegerRange(c ? l + shrMin : l - shrMax, c ? r - shrMax : r + shrMin)
                : null;
    }

    @Nonnull
    public static BlockSurfaceArea expand(BlockSurfaceArea area, BlockSurfacePos lower, BlockSurfacePos higher)
    {
        BlockSurfacePos p1 = area.getPoint1();
        BlockSurfacePos p2 = area.getPoint2();
        IntegerRange x = expand(p1.getX(), p2.getX(), lower.getX(), higher.getX());
        IntegerRange z = expand(p1.getZ(), p2.getZ(), lower.getZ(), higher.getZ());

        return new BlockSurfaceArea(new BlockSurfacePos(x.min, z.min), new BlockSurfacePos(x.max, z.max));
    }

    @Nonnull
    private static IntegerRange expand(int l, int r, int expMin, int expMax)
    {
        boolean c = l < r;
        return new IntegerRange(c ? l - expMin : l + expMax, c ? r + expMax : r - expMin);
    }
}
