/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.maze.*;
import ivorius.ivtoolkit.maze.MazeComponent;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import ivorius.reccomplex.utils.RCMazeGenerator;
import ivorius.reccomplex.worldgen.StructureHandler;
import ivorius.reccomplex.worldgen.genericStructures.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.*;

/**
 * Created by lukas on 06.06.14.
 */
public class TileEntityMazeGenerator extends TileEntity implements GeneratingTileEntity
{
    public String mazeID = "";
    public List<MazePath> mazeExits = new ArrayList<>();
    public List<MazeRoomArea> blockedRoomAreas = new ArrayList<>();

    public BlockCoord structureShift = new BlockCoord(0, 0, 0);

    public int[] roomSize = new int[]{3, 5, 3};
    public int[] roomNumbers = new int[]{4, 1, 4};

    public String getMazeID()
    {
        return mazeID;
    }

    public void setMazeID(String mazeID)
    {
        this.mazeID = mazeID;
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
        mazeID = nbtTagCompound.getString("mazeID");

        mazeExits.clear();
        NBTTagList exitsList = nbtTagCompound.getTagList("mazeExits", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < exitsList.tagCount(); i++)
        {
            mazeExits.add(new MazePath(exitsList.getCompoundTagAt(i)));
        }

        blockedRoomAreas.clear();
        NBTTagList blockedRoomsList = nbtTagCompound.getTagList("blockedRoomAreas", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < blockedRoomsList.tagCount(); i++)
        {
            blockedRoomAreas.add(new MazeRoomArea(blockedRoomsList.getCompoundTagAt(i)));
        }

        structureShift = BlockCoord.readCoordFromNBT("structureShift", nbtTagCompound);

        roomSize = IvNBTHelper.readIntArrayFixedSize("roomSize", 3, nbtTagCompound);
        roomNumbers = IvNBTHelper.readIntArrayFixedSize("roomNumbers", 3, nbtTagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        super.writeToNBT(nbtTagCompound);

        writeMazeDataToNBT(nbtTagCompound);
    }

    public void writeMazeDataToNBT(NBTTagCompound nbtTagCompound)
    {
        nbtTagCompound.setString("mazeID", mazeID);

        NBTTagList exitsList = new NBTTagList();
        for (MazePath exit : mazeExits)
        {
            exitsList.appendTag(exit.writeToNBT());
        }
        nbtTagCompound.setTag("mazeExits", exitsList);

        NBTTagList blockedRoomsList = new NBTTagList();
        for (MazeRoomArea area : blockedRoomAreas)
        {
            blockedRoomsList.appendTag(area.writeToNBT());
        }
        nbtTagCompound.setTag("blockedRoomAreas", blockedRoomsList);

        BlockCoord.writeCoordToNBT("structureShift", structureShift, nbtTagCompound);

        nbtTagCompound.setIntArray("roomSize", roomSize);
        nbtTagCompound.setIntArray("roomNumbers", roomNumbers);
    }

    @Override
    public void generate(World world, Random random, AxisAlignedTransform2D transform, int layer)
    {
        world.setBlockToAir(xCoord, yCoord, zCoord);

        BlockCoord startCoord = structureShift.add(xCoord, yCoord, zCoord);

        Maze maze = new Maze(roomNumbers[0] * 2 + 1, roomNumbers[1] * 2 + 1, roomNumbers[2] * 2 + 1);

        List<MazeComponent> transformedComponents = WorldGenMaze.transformedComponents(StructureHandler.getStructuresInMaze(mazeID));

        Set<MazeRoom> blockedRooms = new HashSet<>();
        for (MazeRoomArea area : blockedRoomAreas)
            blockedRooms.addAll(area.mazeRooms());

        RCMazeGenerator.generateStartPathsForEnclosedMaze(maze, mazeExits, blockedRooms);
        for (int i = 0; i < roomNumbers[0] * roomNumbers[1] * roomNumbers[2] / (5 * 5 * 5) + 1; i++)
        {
            MazePath randPath = RCMazeGenerator.randomEmptyPathInMaze(random, maze);
            if (randPath != null)
                maze.set(Maze.WALL, randPath);
            else
                break;
        }
        List<MazeComponentPosition> placedComponents = MazeGeneratorWithComponents.generatePaths(random, maze, transformedComponents);

        WorldGenMaze.generateMaze(world, random, startCoord, placedComponents, roomSize, layer);
    }
}
