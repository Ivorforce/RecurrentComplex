/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.genericStructures;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.maze.*;
import ivorius.reccomplex.worldgen.StructureInfo;
import ivorius.reccomplex.worldgen.WorldGenStructures;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 27.06.14.
 */
public class WorldGenMaze
{
    public static boolean generateMaze(World world, Random random, BlockCoord coord, List<MazeComponentPosition> placedComponents, int[] roomSize, int layer)
    {
        int[] pathLengths = new int[]{0, 0, 0};

        for (MazeComponentPosition position : placedComponents)
        {
            MazeComponentInfo info = (MazeComponentInfo) position.getComponent().getIdentifier();

            MazeRoom mazePosition = position.getPositionInMaze();
//            int[] size = maze.getRoomSize(mazePosition, pathLengths, roomSize);
            int[] scaledCompMazePosition = Maze.getRoomPosition(mazePosition, pathLengths, roomSize);

            AxisAlignedTransform2D componentTransform = info.transform;
            StructureInfo compStructureInfo = info.structureInfo;

            int[] compStructureSize = WorldGenStructures.structureSize(compStructureInfo, componentTransform);
            int[] compRoomSize = Maze.getRoomSize(position.getComponent().getSize(), pathLengths, roomSize);
            int[] sizeDependentShift = new int[]{(compRoomSize[0] - compStructureSize[0]) / 2, (compRoomSize[1] - compStructureSize[1]) / 2, (compRoomSize[2] - compStructureSize[2]) / 2};

            BlockCoord compMazeCoordLower = coord.add(scaledCompMazePosition[0] + sizeDependentShift[0], scaledCompMazePosition[1] + sizeDependentShift[1], scaledCompMazePosition[2] + +sizeDependentShift[2]);

            WorldGenStructures.generateStructureWithNotifications(compStructureInfo, world, random, compMazeCoordLower, componentTransform, layer + 1, false);
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

    public static List<MazeComponent> transformedComponents(List<StructureInfo> componentStructures)
    {
        List<MazeComponent> transformedComponents = new ArrayList<>();
        for (StructureInfo info : componentStructures)
        {
            SavedMazeComponent comp = info.mazeComponent();

            int[] compSize = comp.getSize();
            int roomVariations = (info.isRotatable() ? 4 : 1) * (info.isMirrorable() ? 2 : 1);

            int splitCompWeight = 0;
            if (comp.itemWeight > 0)
                splitCompWeight = Math.max(1, comp.itemWeight / roomVariations);

            for (int rotations = 0; rotations < (info.isRotatable() ? 4 : 1); rotations++)
            {
                for (int mirrorInd = 0; mirrorInd < (info.isMirrorable() ? 2 : 1); mirrorInd++)
                {
                    AxisAlignedTransform2D componentTransform = AxisAlignedTransform2D.transform(rotations, mirrorInd == 1);

                    List<MazeRoom> transformedRooms = new ArrayList<>();
                    for (MazeRoom room : comp.getRooms())
                        transformedRooms.add(MazeGenerator.rotatedRoom(room, componentTransform, compSize));

                    List<MazePath> transformedExits = new ArrayList<>();
                    for (MazePath exit : comp.getExitPaths())
                        transformedExits.add(MazeGenerator.rotatedPath(exit, componentTransform, compSize));

                    MazeComponentInfo compInfo = new MazeComponentInfo(info, componentTransform);
                    transformedComponents.add(new MazeComponent(splitCompWeight, compInfo, transformedRooms, transformedExits));
                }
            }
        }

        return transformedComponents;
    }

    public static class MazeComponentInfo
    {
        public StructureInfo structureInfo;
        public AxisAlignedTransform2D transform;

        public MazeComponentInfo(StructureInfo structureInfo, AxisAlignedTransform2D transform)
        {
            this.structureInfo = structureInfo;
            this.transform = transform;
        }
    }
}
