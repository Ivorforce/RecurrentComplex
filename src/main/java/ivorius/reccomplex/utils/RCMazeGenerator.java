/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.maze.Maze;
import ivorius.ivtoolkit.maze.MazePath;
import ivorius.ivtoolkit.maze.MazeRoom;
import ivorius.reccomplex.worldgen.genericStructures.WorldGenMaze;

import java.util.*;

/**
 * Created by lukas on 08.10.14.
 */
public class RCMazeGenerator
{
    public static void generateStartPathsForEnclosedMaze(Maze maze, Iterable<MazePath> startPoints, Iterable<MazeRoom> blockedRooms, AxisAlignedTransform2D transform)
    {
        int[] mazeSizeInRooms = new int[maze.dimensions.length];
        for (int i = 0; i < mazeSizeInRooms.length; i++)
            mazeSizeInRooms[i] = (maze.dimensions[i] - 1) / 2;

        for (MazePath path : maze.allPaths())
        {
            if (maze.isPathPointingOutside(path))
                maze.set(Maze.WALL, path);
        }

        for (MazeRoom blockedRoom : blockedRooms)
        {
            blockedRoom = WorldGenMaze.rotatedRoom(blockedRoom, transform, mazeSizeInRooms);

            maze.set(Maze.WALL, blockedRoom);
            for (int dim = 0; dim < maze.dimensions.length; dim++)
            {
                maze.set(Maze.WALL, new MazePath(blockedRoom, dim, true));
                maze.set(Maze.WALL, new MazePath(blockedRoom, dim, false));
            }
        }

        for (MazePath startPoint : startPoints)
            maze.set(Maze.ROOM, WorldGenMaze.rotatedPath(startPoint, transform, mazeSizeInRooms));
    }

    public static MazePath randomEmptyPathInMaze(Random rand, Maze maze, Collection<Integer> applicableDimensions)
    {
        List<MazePath> paths = new ArrayList<>(maze.allPaths());
        for (Iterator<MazePath> iterator = paths.iterator(); iterator.hasNext(); )
        {
            MazePath path = iterator.next();
            if (maze.get(path) != Maze.NULL || !applicableDimensions.contains(path.pathDimension))
                iterator.remove();
        }

        if (paths.size() > 0)
            return paths.get(rand.nextInt(paths.size()));

        return null;
    }
}
