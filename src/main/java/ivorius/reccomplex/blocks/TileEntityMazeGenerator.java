/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.maze.*;
import ivorius.ivtoolkit.maze.MazeComponent;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import ivorius.reccomplex.worldgen.StructureRegistry;
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
    public Selection mazeRooms = new Selection();

    public BlockCoord structureShift = new BlockCoord(0, 0, 0);

    public int[] roomSize = new int[]{3, 5, 3};

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

    public Selection getMazeRooms()
    {
        return mazeRooms;
    }

    public void setMazeRooms(Selection mazeRooms)
    {
        this.mazeRooms = mazeRooms;
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

        NBTTagCompound rooms = nbtTagCompound.getCompoundTag("rooms");
        mazeRooms.readFromNBT(rooms, 3);

        // Legacy
        if (nbtTagCompound.hasKey("roomNumbers", Constants.NBT.TAG_INT_ARRAY))
            mazeRooms.add(new Selection.Area(true, new int[]{0, 0, 0}, IvVecMathHelper.sub(IvNBTHelper.readIntArrayFixedSize("roomNumbers", 3, nbtTagCompound), new int[]{1, 1, 1})));
        if (nbtTagCompound.hasKey("blockedRoomAreas", Constants.NBT.TAG_LIST))
        {
            NBTTagList blockedRoomsList = nbtTagCompound.getTagList("blockedRoomAreas", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < blockedRoomsList.tagCount(); i++)
            {
                NBTTagCompound blockedRoomTag = blockedRoomsList.getCompoundTagAt(i);
                mazeRooms.add(new Selection.Area(false, IvNBTHelper.readIntArrayFixedSize("min", 3, blockedRoomTag), IvNBTHelper.readIntArrayFixedSize("max", 3, blockedRoomTag)));
            }
        }

        mazeExits.clear();
        NBTTagList exitsList = nbtTagCompound.getTagList("mazeExits", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < exitsList.tagCount(); i++)
            mazeExits.add(new MazePath(exitsList.getCompoundTagAt(i)));

        structureShift = BlockCoord.readCoordFromNBT("structureShift", nbtTagCompound);

        roomSize = IvNBTHelper.readIntArrayFixedSize("roomSize", 3, nbtTagCompound);
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

        NBTTagCompound rooms = new NBTTagCompound();
        mazeRooms.writeToNBT(rooms);
        nbtTagCompound.setTag("rooms", rooms);

        NBTTagList exitsList = new NBTTagList();
        for (MazePath exit : mazeExits)
            exitsList.appendTag(exit.writeToNBT());
        nbtTagCompound.setTag("mazeExits", exitsList);

        BlockCoord.writeCoordToNBT("structureShift", structureShift, nbtTagCompound);

        nbtTagCompound.setIntArray("roomSize", roomSize);
    }

    @Override
    public void generate(World world, Random random, AxisAlignedTransform2D transform, int layer)
    {
        world.setBlockToAir(xCoord, yCoord, zCoord);

        int[] roomNumbers = mazeRooms.boundsHigher();

        int[] mazeSize = new int[]{roomSize[0] * roomNumbers[0], roomSize[1] * roomNumbers[1], roomSize[2] * roomNumbers[2]};
        BlockCoord startCoord = transform.apply(structureShift, new int[]{1, 1, 1}).add(xCoord, yCoord, zCoord).subtract(transform.apply(new BlockCoord(0, 0, 0), mazeSize));

        Maze maze = new Maze(roomNumbers[0] * 2 + 1, roomNumbers[1] * 2 + 1, roomNumbers[2] * 2 + 1);

        List<MazeComponent> transformedComponents = WorldGenMaze.transformedComponents(StructureRegistry.getStructuresInMaze(mazeID));
        Set<Integer> pathDims = new HashSet<>();
        for (MazeComponent mazeComponent : transformedComponents)
        {
            for (MazePath path : mazeComponent.getExitPaths())
                pathDims.add(path.pathDimension);
        }

        Collection<MazeRoom> blockedRooms = mazeRooms.mazeRooms(false);

        MazeGenerator.generateStartPathsForEnclosedMaze(maze, mazeExits, blockedRooms, transform);
        for (int i = 0; i < roomNumbers[0] * roomNumbers[1] * roomNumbers[2] / (5 * 5 * 5) + 1; i++)
        {
            MazePath randPath = MazeGenerator.randomEmptyPathInMaze(random, maze, pathDims);
            if (randPath != null)
                maze.set(Maze.ROOM, randPath);
            else
                break;
        }

        List<MazeComponentPosition> placedComponents = MazeGeneratorWithComponents.generatePaths(random, maze, transformedComponents);

        WorldGenMaze.generateMaze(world, random, startCoord, placedComponents, roomSize, layer);
    }
}
