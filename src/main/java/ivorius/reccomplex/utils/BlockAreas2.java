/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.blocks.BlockArea;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by lukas on 05.04.15.
 */
public class BlockAreas2
{
    public static int sideLength(BlockArea area, ForgeDirection side)
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
}
