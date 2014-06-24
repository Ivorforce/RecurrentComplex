/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.blocks;

import ivorius.structuregen.StructureGen;
import ivorius.structuregen.ivtoolkit.blocks.BlockCoord;
import ivorius.structuregen.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.structuregen.ivtoolkit.maze.*;
import ivorius.structuregen.ivtoolkit.tools.IvCollections;
import ivorius.structuregen.worldgen.StructureHandler;
import ivorius.structuregen.worldgen.StructureInfo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 06.06.14.
 */
public class TileEntityMazeGenerator extends TileEntity implements GeneratingTileEntity
{
    public List<MazeComponent> mazeComponents = new ArrayList<>();
    public List<MazePath> mazeExits = new ArrayList<>();

    public BlockCoord structureShift = new BlockCoord(0, 0, 0);

    public int[] roomSize = new int[]{3, 5, 3};
    public int[] roomNumbers = new int[]{4, 1, 4};

    public List<MazeComponent> getMazeComponents()
    {
        return Collections.unmodifiableList(mazeComponents);
    }

    public void setMazeComponents(List<MazeComponent> mazeComponents)
    {
        IvCollections.setContentsOfList(this.mazeComponents, mazeComponents);
    }

    public BlockCoord getStructureShift()
    {
        return structureShift;
    }

    public void setStructureShift(BlockCoord structureShift)
    {
        this.structureShift = structureShift;
    }

    public int[] getRoomSize()
    {
        return roomSize.clone();
    }

    public void setRoomSize(int[] roomSize)
    {
        this.roomSize = roomSize;
    }

    public int[] getRoomNumbers()
    {
        return roomNumbers.clone();
    }

    public void setRoomNumbers(int[] roomNumbers)
    {
        this.roomNumbers = roomNumbers;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        super.readFromNBT(nbtTagCompound);

        readMazeDataFromNBT(nbtTagCompound);
    }

    public void readMazeDataFromNBT(NBTTagCompound nbtTagCompound)
    {
        mazeComponents.clear();
        NBTTagList componentsList = nbtTagCompound.getTagList("mazeComponents", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < componentsList.tagCount(); i++)
        {
            mazeComponents.add(new MazeComponent(componentsList.getCompoundTagAt(i)));
        }

        mazeExits.clear();
        NBTTagList exitsList = nbtTagCompound.getTagList("mazeExits", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < exitsList.tagCount(); i++)
        {
            mazeExits.add(new MazePath(exitsList.getCompoundTagAt(i)));
        }

        structureShift = BlockCoord.readCoordFromNBT("structureShift", nbtTagCompound);

        roomSize = nbtTagCompound.getIntArray("roomSize");
        roomNumbers = nbtTagCompound.getIntArray("roomNumbers");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        super.writeToNBT(nbtTagCompound);

        writeMazeDataToNBT(nbtTagCompound);
    }

    public void writeMazeDataToNBT(NBTTagCompound nbtTagCompound)
    {
        NBTTagList componentList = new NBTTagList();
        for (MazeComponent component : mazeComponents)
        {
            NBTTagCompound compound = new NBTTagCompound();
            component.writeToNBT(compound);
            componentList.appendTag(compound);
        }
        nbtTagCompound.setTag("mazeComponents", componentList);

        NBTTagList exitsList = new NBTTagList();
        for (MazePath exit : mazeExits)
        {
            exitsList.appendTag(exit.writeToNBT());
        }
        nbtTagCompound.setTag("mazeExits", exitsList);

        BlockCoord.writeCoordToNBT("structureShift", structureShift, nbtTagCompound);

        nbtTagCompound.setIntArray("roomSize", roomSize);
        nbtTagCompound.setIntArray("roomNumbers", roomNumbers);
    }

    @Override
    public void generate(World world, Random random, AxisAlignedTransform2D transform, int layer)
    {
        world.setBlockToAir(xCoord, yCoord, zCoord);

        List<MazeComponent> transformedComponents = new ArrayList<>();
        for (MazeComponent comp : mazeComponents)
        {
            StructureInfo info = StructureHandler.getStructure(comp.getIdentifier());

            for (int rotations = 0; rotations < (info.isRotatable() ? 4 : 1); rotations++)
            {
                for (int mirrorInd = 0; mirrorInd < (info.isMirrorable() ? 2 : 1); mirrorInd++)
                {
                    String newID = comp.getIdentifier() + "_" + rotations + "_" + mirrorInd;
                    AxisAlignedTransform2D componentTransform = AxisAlignedTransform2D.transform(rotations, mirrorInd == 1);

                    List<MazeRoom> transformedRooms = new ArrayList<>();
                    for (MazeRoom room : comp.getRooms())
                    {
                        int[] roomPosition = room.coordinates;
                        BlockCoord transformedRoom = componentTransform.apply(new BlockCoord(roomPosition[0], roomPosition[1], roomPosition[2]), new int[3]);
                        transformedRooms.add(new MazeRoom(transformedRoom.x, transformedRoom.y, transformedRoom.z));
                    }

                    List<MazePath> transformedExits = new ArrayList<>();
                    for (MazePath exit : comp.getExitPaths())
                    {
                        int[] sourceCoords = exit.getSourceRoom().coordinates;
                        int[] destCoords = exit.getDestinationRoom().coordinates;
                        BlockCoord transformedSource = componentTransform.apply(new BlockCoord(sourceCoords[0], sourceCoords[1], sourceCoords[2]), new int[3]);
                        BlockCoord transformedDest = componentTransform.apply(new BlockCoord(destCoords[0], destCoords[1], destCoords[2]), new int[3]);

                        transformedExits.add(MazePath.pathFromSourceAndDest(new MazeRoom(transformedSource.x, transformedSource.y, transformedSource.z), new MazeRoom(transformedDest.x, transformedDest.y, transformedDest.z)));
                    }

                    transformedComponents.add(new MazeComponent(comp.itemWeight, newID, transformedRooms, transformedExits));
                }
            }
        }

        int[] pathLengths = new int[]{0, 0, 0};

        Maze maze = new Maze(roomNumbers[0] * 2 + 1, roomNumbers[1] * 2 + 1, roomNumbers[2] * 2 + 1);

        MazePath[] mazeExits = new MazePath[1 + this.mazeExits.size()];
        mazeExits[0] = MazeGenerator.randomPathInMaze(random, maze, 1, 1, 1);
        for (int i = 0; i < this.mazeExits.size(); i++)
        {
            mazeExits[i + 1] = this.mazeExits.get(i);
        }

        MazeGenerator.generateStartPathsForEnclosedMaze(maze, mazeExits);

        List<MazeComponentPosition> placedComponents = MazeGeneratorWithComponents.generatePaths(random, maze, transformedComponents);
        BlockCoord startCoord = structureShift.add(xCoord, yCoord, zCoord);

        for (MazeComponentPosition position : placedComponents)
        {
            String identifier = position.getComponent().getIdentifier();
            int splitIndex0 = identifier.lastIndexOf("_");
            boolean mirror = Integer.valueOf(identifier.substring(splitIndex0 + 1)) == 1;
            int splitIndex1 = identifier.lastIndexOf("_", splitIndex0 - 1);
            String structure = identifier.substring(0, splitIndex1);
            int rotations = Integer.valueOf(identifier.substring(splitIndex1 + 1, splitIndex0));

            MazeRoom mazePosition = position.getPositionInMaze();
//            int[] size = maze.getRoomSize(mazePosition, pathLengths, roomSize);
            int[] scaledMazePosition = maze.getRoomPosition(mazePosition, pathLengths, roomSize);

            AxisAlignedTransform2D componentTransform = AxisAlignedTransform2D.transform(rotations, mirror);
            BlockCoord mazeCoordLower = startCoord.add(scaledMazePosition[0], scaledMazePosition[1], scaledMazePosition[2]);

            StructureInfo structureInfo = StructureHandler.getStructure(structure);

            if (structureInfo != null)
            {
                structureInfo.generate(world, random, mazeCoordLower, componentTransform, layer + 1);
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
    }
}
