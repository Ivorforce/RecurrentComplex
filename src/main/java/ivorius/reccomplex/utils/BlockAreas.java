/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import net.minecraftforge.common.util.ForgeDirection;

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
        BlockCoord lowerCorner = area.getLowerCorner().add(lower);
        BlockCoord higherCorner = area.getHigherCorner().subtract(higher);
        return lowerCorner.x <= higherCorner.x && lowerCorner.y <= higherCorner.y && lowerCorner.z <= higherCorner.z
                ? new BlockArea(lowerCorner, higherCorner)
                : null;
    }
}
