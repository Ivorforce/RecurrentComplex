/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.gui.IntegerRange;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by lukas on 28.03.15.
 */
public class BlockAreas
{
    public static final BlockCoord ZERO_COORD = new BlockCoord(0, 0, 0);

    public static BlockArea side(BlockArea area, ForgeDirection side)
    {
        BlockCoord lowerCorner = area.getLowerCorner();
        BlockCoord higherCorner = area.getHigherCorner();

        switch (side)
        {
            case UP:
                return new BlockArea(new BlockCoord(lowerCorner.x, higherCorner.y, lowerCorner.z), higherCorner);
            case DOWN:
                return new BlockArea(lowerCorner, new BlockCoord(higherCorner.x, lowerCorner.y, higherCorner.z));
            case NORTH:
                return new BlockArea(lowerCorner, new BlockCoord(higherCorner.x, higherCorner.y, lowerCorner.z));
            case EAST:
                return new BlockArea(new BlockCoord(higherCorner.x, lowerCorner.y, lowerCorner.z), higherCorner);
            case SOUTH:
                return new BlockArea(new BlockCoord(lowerCorner.x, lowerCorner.y, higherCorner.z), higherCorner);
            case WEST:
                return new BlockArea(lowerCorner, new BlockCoord(lowerCorner.x, higherCorner.y, higherCorner.z));
            default:
                throw new IllegalArgumentException();
        }
    }

    @Nullable
    public static BlockArea shrink(BlockArea area, ForgeDirection side, int amount)
    {
        switch (side)
        {
            case UP:
                return shrink(area, ZERO_COORD, new BlockCoord(0, amount, 0));
            case DOWN:
                return shrink(area, new BlockCoord(0, amount, 0), ZERO_COORD);
            case NORTH:
                return shrink(area, new BlockCoord(0, 0, amount), ZERO_COORD);
            case EAST:
                return shrink(area, ZERO_COORD, new BlockCoord(amount, 0, 0));
            case SOUTH:
                return shrink(area, ZERO_COORD, new BlockCoord(0, 0, amount));
            case WEST:
                return shrink(area, new BlockCoord(amount, 0, 0), ZERO_COORD);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Nullable
    public static BlockArea shrink(BlockArea area, BlockCoord lower, BlockCoord higher)
    {
        BlockCoord p1 = area.getPoint1();
        BlockCoord p2 = area.getPoint2();
        IntegerRange x = shrink(p1.x, p2.x, lower.x, higher.x);
        IntegerRange y = shrink(p1.y, p2.y, lower.y, higher.y);
        IntegerRange z = shrink(p1.z, p2.z, lower.z, higher.z);

        return x != null && y != null && z != null
                ? new BlockArea(new BlockCoord(x.min, y.min, z.min), new BlockCoord(x.max, y.max, z.max))
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
    public static BlockArea expand(BlockArea area, BlockCoord lower, BlockCoord higher)
    {
        BlockCoord p1 = area.getPoint1();
        BlockCoord p2 = area.getPoint2();
        IntegerRange x = expand(p1.x, p2.x, lower.x, higher.x);
        IntegerRange y = expand(p1.y, p2.y, lower.y, higher.y);
        IntegerRange z = expand(p1.z, p2.z, lower.z, higher.z);

        return new BlockArea(new BlockCoord(x.min, y.min, z.min), new BlockCoord(x.max, y.max, z.max));
    }

    @Nonnull
    private static IntegerRange expand(int l, int r, int expMin, int expMax)
    {
        boolean c = l < r;
        return new IntegerRange(c ? l - expMin : l + expMax, c ? r + expMax : r - expMin);
    }
}
