/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.worldgen.genericStructures;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.maze.*;
import ivorius.structuregen.StructureGen;
import ivorius.structuregen.worldgen.StructureHandler;
import ivorius.structuregen.worldgen.StructureInfo;
import ivorius.structuregen.worldgen.WorldGenStructures;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 27.06.14.
 */
public class WorldGenMaze
{
    public static boolean generateMaze(World world, Random random, BlockCoord coord, List<MazeComponentPosition> placedComponents,  int[] roomSize, int layer)
    {
        int[] pathLengths = new int[]{0, 0, 0};

        for (MazeComponentPosition position : placedComponents)
        {
            String identifier = position.getComponent().getIdentifier();
            int splitIndex0 = identifier.lastIndexOf("_");
            boolean mirror = Boolean.valueOf(identifier.substring(splitIndex0 + 1));
            int splitIndex1 = identifier.lastIndexOf("_", splitIndex0 - 1);
            String structure = identifier.substring(0, splitIndex1);
            int rotations = Integer.valueOf(identifier.substring(splitIndex1 + 1, splitIndex0));

            MazeRoom mazePosition = position.getPositionInMaze();
//            int[] size = maze.getRoomSize(mazePosition, pathLengths, roomSize);
            int[] scaledCompMazePosition = Maze.getRoomPosition(mazePosition, pathLengths, roomSize);

            AxisAlignedTransform2D componentTransform = AxisAlignedTransform2D.transform(rotations, mirror);
            StructureInfo compStructureInfo = StructureHandler.getStructure(structure);

            if (compStructureInfo != null)
            {
                int[] compStructureSize = WorldGenStructures.structureBoundingBox(compStructureInfo, componentTransform);
                int[] compRoomSize = Maze.getRoomSize(position.getComponent().getSize(), pathLengths, roomSize);
                int[] sizeDependentShift = new int[]{(compRoomSize[0] - compStructureSize[0]) / 2, (compRoomSize[1] - compStructureSize[1]) / 2, (compRoomSize[2] - compStructureSize[2]) / 2};

                BlockCoord compMazeCoordLower = coord.add(scaledCompMazePosition[0] + sizeDependentShift[0], scaledCompMazePosition[1] + sizeDependentShift[1], scaledCompMazePosition[2] +  + sizeDependentShift[2]);

                compStructureInfo.generate(world, random, compMazeCoordLower, componentTransform, layer + 1);
            }
            else
            {
                StructureGen.logger.error("Could not find maze component structure '" + structure + "'");
            }
        }

//        for (int i = 0; i < maze.blocks.length; i++)
//        {
//            byte blockType = maze.blocks[i];
//
//            int[] mazePosition = maze.getCoordPosition(i);
//
//            int[] size = maze.getRoomSize(mazePosition, pathLengths, roomSize);
//
//            if (size[0] > 0 && size[1] > 0 && size[2] > 0)
//            {
//                int[] scaledMazePosition = maze.getRoomPosition(mazePosition, pathLengths, roomSize);
//
//                BlockCoord mazeCoordLower = startCoord.add(scaledMazePosition[0], scaledMazePosition[1], scaledMazePosition[2]);
//                BlockCoord mazeCoordHigher = mazeCoordLower.add(size[0] - 1, size[1] - 1, size[2] - 1);
//
//                for (BlockCoord worldCoord : new BlockArea(mazeCoordLower, mazeCoordHigher))
//                {
//                    if (blockType == Maze.WALL)
//                    {
//                        world.setBlock(worldCoord.x, worldCoord.y, worldCoord.z, Blocks.stone);
//                    }
//                    else if (blockType == Maze.ROOM)
//                    {
//                        world.setBlockToAir(worldCoord.x, worldCoord.y, worldCoord.z);
//                    }
//                    else if (blockType == Maze.NULL)
//                    {
//                        world.setBlock(worldCoord.x, worldCoord.y, worldCoord.z, SGBlocks.negativeSpace);
//                    }
//                    else if (blockType == Maze.INVALID)
//                    {
//                        world.setBlock(worldCoord.x, worldCoord.y, worldCoord.z, Blocks.glass);
//                    }
//                }
//            }
//        }

        return true;
    }

    public static List<MazeComponent> transformedComponents(List<MazeComponent> rawComponents)
    {
        List<MazeComponent> transformedComponents = new ArrayList<>();
        for (MazeComponent comp : rawComponents)
        {
            StructureInfo info = StructureHandler.getStructure(comp.getIdentifier());
            int[] compSize = comp.getSize();
            int roomVariations = (info.isRotatable() ? 4 : 1) * (info.isMirrorable() ? 2 : 1);

            int splitCompWeight = 0;
            if (comp.itemWeight > 0)
            {
                splitCompWeight = Math.max(1, comp.itemWeight / roomVariations);
            }

            for (int rotations = 0; rotations < (info.isRotatable() ? 4 : 1); rotations++)
            {
                for (int mirrorInd = 0; mirrorInd < (info.isMirrorable() ? 2 : 1); mirrorInd++)
                {
                    String newID = comp.getIdentifier() + "_" + rotations + "_" + (mirrorInd == 1);
                    AxisAlignedTransform2D componentTransform = AxisAlignedTransform2D.transform(rotations, mirrorInd == 1);

                    List<MazeRoom> transformedRooms = new ArrayList<>();
                    for (MazeRoom room : comp.getRooms())
                    {
                        int[] roomPosition = room.coordinates;
                        BlockCoord transformedRoom = componentTransform.apply(new BlockCoord(roomPosition[0], roomPosition[1], roomPosition[2]), compSize);
                        transformedRooms.add(new MazeRoom(transformedRoom.x, transformedRoom.y, transformedRoom.z));
                    }

                    List<MazePath> transformedExits = new ArrayList<>();
                    for (MazePath exit : comp.getExitPaths())
                    {
                        int[] sourceCoords = exit.getSourceRoom().coordinates;
                        int[] destCoords = exit.getDestinationRoom().coordinates;
                        BlockCoord transformedSource = componentTransform.apply(new BlockCoord(sourceCoords[0], sourceCoords[1], sourceCoords[2]), compSize);
                        BlockCoord transformedDest = componentTransform.apply(new BlockCoord(destCoords[0], destCoords[1], destCoords[2]), compSize);

                        transformedExits.add(MazePath.pathFromSourceAndDest(new MazeRoom(transformedSource.x, transformedSource.y, transformedSource.z), new MazeRoom(transformedDest.x, transformedDest.y, transformedDest.z)));
                    }

                    transformedComponents.add(new MazeComponent(splitCompWeight, newID, transformedRooms, transformedExits));
                }
            }
        }

        return transformedComponents;
    }
}
