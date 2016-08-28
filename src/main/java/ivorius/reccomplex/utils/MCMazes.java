/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.maze.components.MazeRoom;
import net.minecraft.util.math.BlockPos;

/**
 * Created by lukas on 28.08.16.
 */
public class MCMazes
{
    public static MazeRoom mazeRoom(BlockPos pos)
    {
        return new MazeRoom(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos blockPos(MazeRoom room)
    {
        if (room.getDimensions() != 3)
            throw new IllegalArgumentException();

        return new BlockPos(room.getCoordinate(0), room.getCoordinate(1), room.getCoordinate(2));
    }
}
