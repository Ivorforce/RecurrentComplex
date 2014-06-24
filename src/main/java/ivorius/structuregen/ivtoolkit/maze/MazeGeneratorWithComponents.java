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

import ivorius.structuregen.StructureGen;
import net.minecraft.util.WeightedRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * Created by lukas on 20.06.14.
 */
public class MazeGeneratorWithComponents
{
    public static List<MazeComponentPosition> generatePaths(Random rand, Maze maze, List<MazeComponent> mazeComponents)
    {
        List<MazeComponentPosition> positions = new ArrayList<>();

        Stack<MazeRoom> positionStack = new Stack<>();

        System.out.println("maze.allPaths() = " + maze.allPaths());
        System.out.println("maze.allPaths().size() = " + maze.allPaths().size());
        // Gather needed start points
        for (MazePath path : maze.allPaths())
        {
            if (maze.get(path) == Maze.ROOM)
            {
                positionStack.push(path.getSourceRoom());
                positionStack.push(path.getDestinationRoom());
            }
        }

        ArrayList<MazeComponentPosition> validComponents = new ArrayList<>();

        System.out.println("positionStack = " + positionStack);
        System.out.println("positionStack = " + positionStack.size());
        while (!positionStack.empty())
        {
            maze.logMaze2D(StructureGen.logger, 0, 2, new MazeCoordinateDirect(0, 1, 0));
            MazeRoom position = positionStack.pop();
            validComponents.clear();

            for (MazeComponent component : mazeComponents)
            {
                for (MazeRoom attachedRoom : component.getRooms())
                {
                    MazeRoom componentPosition = position.sub(attachedRoom);

                    if (canComponentBePlaced(maze, new MazeComponentPosition(component, componentPosition)))
                    {
                        validComponents.add(new MazeComponentPosition(component, componentPosition));
                    }
                }
            }

            if (validComponents.size() == 0)
            {
                StructureGen.logger.debug("Did not find fitting component for maze!");

                continue;
            }

            boolean allZero = true;
            for (MazeComponentPosition component : validComponents)
            {
                if (component.getComponent().itemWeight > 0)
                {
                    allZero = false;
                    break;
                }
            }

            MazeComponentPosition generatingComponent;
            if (allZero)
            {
                generatingComponent = validComponents.get(rand.nextInt(validComponents.size()));
            }
            else
            {
                generatingComponent = (MazeComponentPosition) WeightedRandom.getRandomItem(rand, validComponents);
            }

            for (MazeRoom room : generatingComponent.getComponent().getRooms())
            {
                MazeRoom roomInMaze = generatingComponent.getPositionInMaze().add(room);
                maze.set(Maze.ROOM, roomInMaze);

                MazePath[] neighbors = Maze.getNeighborPaths(maze.dimensions.length, roomInMaze);
                for (MazePath neighbor : neighbors)
                {
                    if (maze.get(neighbor) == Maze.NULL)
                    {
                        maze.set(Maze.WALL, neighbor);
                    }
                }
            }

            for (MazePath exit : generatingComponent.getComponent().getExitPaths())
            {
                MazePath exitInMaze = exit.add(generatingComponent.getPositionInMaze());
                MazeRoom destRoom = exitInMaze.getDestinationRoom();
                MazeRoom srcRoom = exitInMaze.getSourceRoom();

                if (maze.get(destRoom) == Maze.NULL)
                {
                    positionStack.push(destRoom);
                }

                if (maze.get(srcRoom) == Maze.NULL)
                {
                    positionStack.push(srcRoom);
                }

                maze.set(Maze.ROOM, exitInMaze);
            }

            positions.add(generatingComponent);
        }

        return positions;
    }

    public static boolean canComponentBePlaced(Maze maze, MazeComponentPosition component)
    {
        for (MazeRoom room : component.getComponent().getRooms())
        {
            MazeRoom roomInMaze = room.add(component.getPositionInMaze());
            byte curValue = maze.get(roomInMaze);

            if (curValue != Maze.NULL)
            {
                return false;
            }

            MazePath[] roomNeighborPaths = Maze.getNeighborPaths(maze.dimensions.length, roomInMaze);
            for (MazePath roomNeighborPath : roomNeighborPaths)
            {
                byte neighborValue = maze.get(roomNeighborPath);
                if (neighborValue == Maze.ROOM && !component.getComponent().getExitPaths().contains(roomNeighborPath.sub(component.getPositionInMaze())))
                {
                    return false;
                }
            }
        }

        for (MazePath exit : component.getComponent().getExitPaths())
        {
            byte curValue = maze.get(exit.add(component.getPositionInMaze()));

            if (curValue != Maze.ROOM && curValue != Maze.NULL && curValue != Maze.INVALID)
            {
                return false;
            }
        }

        return true;
    }
}
