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

package ivorius.ivtoolkit.maze;

import ivorius.ivtoolkit.math.IvVecMathHelper;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Maze
{
    private static Map<Integer, MazeRoom[]> cachedNeighborRoomsBlueprints = new HashMap<>();
    private static Map<Integer, MazePath[]> cachedNeighborPathBlueprints = new HashMap<>();

    public static final byte NULL = 0;
    public static final byte INVALID = 1;
    public static final byte WALL = 2;
    public static final byte ROOM = 3;

    public final int[] dimensions;
    public final byte[] blocks;

    private List<MazeRoom> cachedRooms;
    private List<MazePath> cachedPaths;

    public Maze(int... dimensions)
    {
        int fullLength = 1;
        for (int i = 0; i < dimensions.length; i++)
        {
            if (dimensions[i] % 2 == 0)
            {
                throw new IllegalArgumentException("Maze must have enclosing walls! (Odd dimension numbers)");
            }

            fullLength *= dimensions[i];
        }

        this.dimensions = dimensions;
        this.blocks = new byte[fullLength];
    }

    public boolean contains(MazeCoordinate coordinate)
    {
        int[] position = coordinate.getMazeCoordinates();

        for (int i = 0; i < position.length; i++)
        {
            if (position[i] < 0 || position[i] >= this.dimensions[i])
            {
                return false;
            }
        }

        return true;
    }

    public byte get(MazeCoordinate coordinate)
    {
        return contains(coordinate) ? this.blocks[getArrayPosition(coordinate.getMazeCoordinates())] : Maze.INVALID;
    }

    public void set(byte val, MazeCoordinate coordinates)
    {
        if (contains(coordinates))
        {
            this.blocks[getArrayPosition(coordinates.getMazeCoordinates())] = val;
        }
    }

    public static int[] getMazeSize(int size[], int pathLengths[], int roomWidths[])
    {
        int[] returnSize = new int[size.length];

        for (int i = 0; i < returnSize.length; i++)
        {
            returnSize[i] = (size[i] - pathLengths[i]) / (pathLengths[i] + roomWidths[i]) * 2 + 1;
        }

        return returnSize;
    }

    public int[] getRoomPosition(MazeCoordinate coordinate, int[] pathLengths, int[] roomWidths)
    {
        int[] mazePosition = coordinate.getMazeCoordinates();
        int[] returnPos = new int[this.dimensions.length];

        for (int i = 0; i < returnPos.length; i++)
        {
            returnPos[i] = (mazePosition[i] / 2) * roomWidths[i] + ((mazePosition[i] + 1) / 2) * pathLengths[i];
        }

        return returnPos;
    }

    public int[] getCompleteMazeSize(int[] pathLengths, int[] roomWidths)
    {
        return getRoomPosition(new MazeRoom(this.dimensions), pathLengths, roomWidths);
    }

    public int[] getRoomSize(MazeCoordinate coordinate, int[] pathLengths, int[] roomWidths)
    {
        int[] returnSize = new int[this.dimensions.length];
        boolean[] isRoomPath = coordPathFlags(coordinate);

        for (int i = 0; i < returnSize.length; i++)
        {
            returnSize[i] = isRoomPath[i] ? pathLengths[i] : roomWidths[i];
        }

        return returnSize;
    }

    public static boolean isCoordValidRoom(MazeCoordinate coordinate)
    {
        boolean[] isRoomPath = coordPathFlags(coordinate);
        for (boolean b : isRoomPath)
        {
            if (b)
            {
                return false;
            }
        }

        return true;
    }

    public static int getPathDimensionIfPath(MazeCoordinate coordinate)
    {
        boolean[] pathFlags = coordPathFlags(coordinate);
        int curDimension = -1;

        for (int dim = 0; dim < pathFlags.length; dim++)
        {
            if (pathFlags[dim])
            {
                if (curDimension >= 0)
                {
                    return -1;
                }

                curDimension = dim;
            }
        }

        return curDimension;
    }

    public static boolean[] coordPathFlags(MazeCoordinate coordinate)
    {
        int[] mazePosition = coordinate.getMazeCoordinates();
        boolean[] flags = new boolean[mazePosition.length];

        for (int i = 0; i < flags.length; i++)
        {
            flags[i] = mazePosition[i] % 2 == 0;
        }

        return flags;
    }

    public boolean isPathPointingOutside(MazeCoordinate coordinate)
    {
        int[] mazePosition = coordinate.getMazeCoordinates();

        for (int dim = 0; dim < dimensions.length; dim++)
        {
            if (mazePosition[dim] == 0 || mazePosition[dim] == dimensions[dim] - 1)
            {
                return true;
            }
        }

        return false;
    }

    public int[] getCoordPosition(int arrayPosition)
    {
        int[] coordPosition = new int[this.dimensions.length];

        for (int dimension = 0; dimension < this.dimensions.length; dimension++)
        {
            coordPosition[dimension] = arrayPosition % this.dimensions[dimension];
            arrayPosition /= this.dimensions[dimension];
        }

        return coordPosition;
    }

    public int getArrayPosition(int... coordPosition)
    {
        int arrayPosition = 0;

        int multiplier = 1;
        for (int dimension = 0; dimension < this.dimensions.length; dimension++)
        {
            arrayPosition += coordPosition[dimension] * multiplier;
            multiplier *= this.dimensions[dimension];
        }

        return arrayPosition;
    }

    public void logMaze2D(Logger logger, int dimension1, int dimension2, MazeCoordinate layerPositon)
    {
        int[] layerPositionArray = layerPositon.getMazeCoordinates();
        StringBuilder mazeString = new StringBuilder();

        for (int dim = 0; dim < dimensions.length; dim++)
        {
            if (dim != dimension1 && dim != dimension2 && (dim < 0 || layerPositionArray[dim] >= dimensions[dim]))
            {
                throw new IllegalArgumentException();
            }
        }

        for (int x = 0; x < this.dimensions[dimension1]; x++)
        {
            if (x > 0)
            {
                mazeString.append("\n");
            }

            for (int y = 0; y < this.dimensions[dimension2]; y++)
            {
                layerPositionArray[dimension1] = x;
                layerPositionArray[dimension2] = y;
                byte type = this.blocks[getArrayPosition(layerPositionArray)];

                if (type == WALL)
                {
                    mazeString.append("#");
                }
                else if (type == INVALID)
                {
                    mazeString.append("*");
                }
                else if (type == NULL)
                {
                    mazeString.append("+");
                }
                else if (type == ROOM)
                {
                    if (this.dimensions.length > 2)
                    {
                        int curDominantDimension = -1;
                        boolean dominantGoesUp = false;

                        for (int refDim = 0; refDim < dimensions.length; refDim++)
                        {
                            if (refDim != dimension1 && refDim != dimension2)
                            {
                                int[] abovePos = layerPositionArray.clone();
                                abovePos[refDim] += 1;
                                int[] belowPos = layerPositionArray.clone();
                                belowPos[refDim] -= 1;

                                byte above = get(new MazeCoordinateDirect(abovePos));
                                byte below = get(new MazeCoordinateDirect(belowPos));

                                if (above == ROOM || below == ROOM)
                                {
                                    if (curDominantDimension >= 0 || above == below)
                                    {
                                        curDominantDimension = -2;
                                        break;
                                    }
                                    else
                                    {
                                        curDominantDimension = refDim;
                                        dominantGoesUp = above == ROOM;
                                    }
                                }
                            }
                        }

                        switch (curDominantDimension)
                        {
                            case -2:
                                mazeString.append('?');
                                break;
                            case -1:
                                mazeString.append(' ');
                                break;
                            default:
                                mazeString.append((char) ((dominantGoesUp ? 'A' : 'a') + curDominantDimension));
                                break;
                        }
                    }
                }
                else
                {
                    mazeString.append(" ");
                }
            }
        }

        logger.info(mazeString.toString());
    }

    public static MazeRoom[] getNeighborRooms(int dimensions)
    {
        if (!cachedNeighborRoomsBlueprints.containsKey(dimensions))
        {
            MazePath[] neighborPaths = getNeighborPaths(dimensions);
            MazeRoom[] neighbors = new MazeRoom[neighborPaths.length];

            for (int i = 0; i < neighborPaths.length; i++)
            {
                neighbors[i] = neighborPaths[i].getDestinationRoom();
            }

            cachedNeighborRoomsBlueprints.put(dimensions, neighbors);
        }

        return cachedNeighborRoomsBlueprints.get(dimensions).clone();
    }

    public static MazePath[] getNeighborPaths(int dimensions)
    {
        if (!cachedNeighborPathBlueprints.containsKey(dimensions))
        {
            MazePath[] neighbors = new MazePath[dimensions * 2];

            for (int i = 0; i < dimensions; i++)
            {
                neighbors[i * 2] = new MazePath(new MazeRoom(new int[dimensions]), i, true);
                neighbors[i * 2 + 1] = new MazePath(new MazeRoom(new int[dimensions]), i, false);
            }

            cachedNeighborPathBlueprints.put(dimensions, neighbors);
        }

        return cachedNeighborPathBlueprints.get(dimensions);
    }

    public static MazePath[] getNeighborPaths(int dimensions, MazeRoom mazeRoom)
    {
        MazePath[] blueprints = getNeighborPaths(dimensions);
        MazePath[] neighbors = new MazePath[blueprints.length];

        for (int i = 0; i < blueprints.length; i++)
        {
            neighbors[i] = new MazePath(blueprints[i].pathDimension, blueprints[i].pathGoesUp, IvVecMathHelper.add(blueprints[i].sourceRoom.coordinates, mazeRoom.coordinates));
        }

        return neighbors;
    }

    public static MazeRoom coordToRoom(MazeCoordinate coordinate)
    {
        if (isCoordValidRoom(coordinate))
        {
            int[] roomCoord = coordinate.getMazeCoordinates();

            for (int dim = 0; dim < roomCoord.length; dim++)
            {
                roomCoord[dim] = (roomCoord[dim] - 1) / 2;
            }

            return new MazeRoom(roomCoord);
        }

        return null;
    }

    public static MazePath coordToPath(MazeCoordinate coordinate)
    {
        int pathDim = getPathDimensionIfPath(coordinate);
        return pathDim >= 0 ? coordToPath(coordinate, pathDim) : null;
    }

    public static MazePath coordToPath(MazeCoordinate coordinate, int pathDim)
    {
        int[] roomCoord = coordinate.getMazeCoordinates();
        boolean goesUp = true;

        for (int dim = 0; dim < roomCoord.length; dim++)
        {
            if (roomCoord[dim] == 0)
            {
                goesUp = false;
            }
            else
            {
                roomCoord[dim] = (roomCoord[dim] - 1) / 2;
            }
        }

        return new MazePath(pathDim, goesUp, roomCoord);
    }

    public List<MazeRoom> allRooms()
    {
        if (cachedRooms == null)
        {
            List<MazeRoom> coordinates = new ArrayList<>();

            for (int i = 0; i < blocks.length; i++)
            {
                int[] coord = getCoordPosition(i);
                MazeRoom room = coordToRoom(new MazeCoordinateDirect(coord));

                if (room != null)
                {
                    coordinates.add(room);
                }
            }

            cachedRooms = coordinates;
        }

        return cachedRooms;
    }

    public List<MazePath> allPaths()
    {
        if (cachedPaths == null)
        {
            List<MazePath> coordinates = new ArrayList<>();

            for (int i = 0; i < blocks.length; i++)
            {
                int[] coord = getCoordPosition(i);
                MazePath path = coordToPath(new MazeCoordinateDirect(coord));

                if (path != null)
                {
                    coordinates.add(path);
                }
            }

            cachedPaths = coordinates;
        }

        return cachedPaths;
    }
}