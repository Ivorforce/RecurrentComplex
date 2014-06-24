/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.structuregen.ivtoolkit.maze;

import net.minecraft.util.WeightedRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class MazeGenerator
{
    public static MazeRoom randomRoomInMaze(Random rand, Maze maze, int... distanceFromOutside)
    {
        int[] position = new int[maze.dimensions.length];
        for (int i = 0; i < maze.dimensions.length; i++)
        {
            position[i] = rand.nextInt(maze.dimensions[i] / 2 - distanceFromOutside[i]);
        }

        return new MazeRoom(position);
    }

    public static MazePath randomPathInMaze(Random rand, Maze maze, int... distanceFromOutside)
    {
        List<WeightedIndex> dimensionWeights = new ArrayList<>();
        for (int dim = 0; dim < maze.dimensions.length; dim++)
        {
            int dimLength = (maze.dimensions[dim] / 2) + 1 - ((distanceFromOutside[dim] + 1) / 2) * 2;
            dimensionWeights.add(new WeightedIndex(Math.max(0, dimLength), dim));
        }
        int usedDimension = ((WeightedIndex) WeightedRandom.getRandomItem(rand, dimensionWeights)).getIndex();

        int[] roomDistanceFromOutside = new int[distanceFromOutside.length];
        for (int i = 0; i < roomDistanceFromOutside.length; i++)
        {
            roomDistanceFromOutside[i] = distanceFromOutside[i] / 2;
        }
        MazeRoom refRoom = randomRoomInMaze(rand, maze, roomDistanceFromOutside);
        int[] pathCoord = refRoom.getMazeCoordinates();
        int availablePaths = ((distanceFromOutside[usedDimension] + 1) / 2) * 2;
        pathCoord[usedDimension] = rand.nextInt(availablePaths) * 2 + (maze.dimensions[usedDimension] / 2 + 1 - availablePaths) / 2;

        return Maze.coordToPath(new MazeCoordinateDirect(pathCoord), usedDimension);
    }

    public static void generatePaths(Random rand, Maze maze, int[] pathWeights, MazeRoom startPoint)
    {
        for (int i = 0; i < maze.dimensions.length; i++)
        {
            if (maze.dimensions[i] < 3)
            {
                return;
            }
        }

        MazeRoom position = startPoint.clone();
        maze.set(Maze.ROOM, position);

        Stack<MazeRoom> positionStack = new Stack<>();

        ArrayList<MazePath> validRoomNeighbors = new ArrayList<>();

        while (true)
        {
            validRoomNeighbors.clear();

            for (MazePath neighbor : Maze.getNeighborPaths(maze.dimensions.length, position))
            {
                if (maze.get(neighbor.getDestinationRoom()) == Maze.NULL)
                {
                    for (int n = 0; n < pathWeights[neighbor.pathDimension]; n++)
                    {
                        validRoomNeighbors.add(neighbor);
                    }
                }
            }

            if (validRoomNeighbors.size() == 0)
            {
                if (positionStack.empty())
                {
                    break;
                }

                position = positionStack.pop();

                continue;
            }

            positionStack.push(position.clone());

            MazePath usedPath = validRoomNeighbors.get(rand.nextInt(validRoomNeighbors.size()));
            MazeRoom destRoom = usedPath.getDestinationRoom();

            maze.set(Maze.ROOM, usedPath);
            maze.set(Maze.ROOM, destRoom);

            MazePath[] neighbors = Maze.getNeighborPaths(maze.dimensions.length, position);
            for (MazePath neighbor : neighbors)
            {
                if (maze.get(neighbor) == Maze.NULL)
                {
                    maze.set(Maze.WALL, neighbor);
                }
            }

            position = destRoom;
        }

        for (int i = 0; i < maze.blocks.length; i++)
        {
            if (maze.blocks[i] == Maze.INVALID || maze.blocks[i] == Maze.NULL)
            {
                maze.blocks[i] = Maze.WALL; //Should not happen. Potentially.
            }
        }
    }

    public static void addRandomPaths(Maze maze, int paths, Random rand)
    {
        for (int i = 0; i < maze.dimensions.length; i++)
        {
            if ((maze.dimensions[i] - 2) / 2 <= 0)
            {
                return;
            }
        }

        int[] distFromOutside = new int[maze.dimensions.length];
        for (; paths > 0; paths--)
        {
            MazePath position = randomPathInMaze(rand, maze, distFromOutside);
            maze.set(Maze.ROOM, position);
        }
    }

    public static void generateStartPathsForEnclosedMaze(Maze maze, MazePath... startPoints)
    {
        for (MazePath path : maze.allPaths())
        {
            if (maze.isPathPointingOutside(path))
            {
                maze.set(Maze.WALL, path);
            }
        }

        for (MazePath startPoint : startPoints)
        {
            maze.set(Maze.ROOM, startPoint);
        }
    }
}