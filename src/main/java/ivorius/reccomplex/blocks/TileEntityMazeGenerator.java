/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.maze.*;
import ivorius.ivtoolkit.maze.MazeComponent;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import ivorius.reccomplex.gui.editmazeblock.GuiEditMazeBlock;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.Selection;
import ivorius.reccomplex.structures.generic.WorldGenMaze;
import ivorius.reccomplex.structures.StructureRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.*;

/**
 * Created by lukas on 06.06.14.
 */
public class TileEntityMazeGenerator extends TileEntity implements GeneratingTileEntity, TileEntityWithGUI
{
    public String mazeID = "";
    public List<MazePath> mazeExits = new ArrayList<>();
    public Selection mazeRooms = Selection.zeroSelection(3);

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

        readSyncedNBT(nbtTagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        super.writeToNBT(nbtTagCompound);

        writeSyncedNBT(nbtTagCompound);
    }

    @Override
    public void generate(StructureSpawnContext context)
    {
        World world = context.world;
        Random random = context.random;
        AxisAlignedTransform2D transform = context.transform;
        int layer = context.generationLayer;

        if (context.includes(xCoord, yCoord, zCoord))
            world.setBlockToAir(xCoord, yCoord, zCoord);

        List<MazeComponentPosition> placedComponents = getPlacedRooms(random, transform);
        if (placedComponents == null)
            return;

        int[] roomNumbers = IvVecMathHelper.add(mazeRooms.boundsHigher(), new int[]{1, 1, 1});

        int[] mazeSize = new int[]{roomSize[0] * roomNumbers[0], roomSize[1] * roomNumbers[1], roomSize[2] * roomNumbers[2]};
        BlockCoord startCoord = transform.apply(structureShift, new int[]{1, 1, 1}).add(xCoord, yCoord, zCoord).subtract(transform.apply(new BlockCoord(0, 0, 0), mazeSize));

        WorldGenMaze.generateMaze(world, random, startCoord, placedComponents, roomSize, layer, context.generationBB, context.isFirstTime);
    }

    public List<MazeComponentPosition> getPlacedRooms(Random random, AxisAlignedTransform2D transform)
    {
        if (mazeRooms.isEmpty())
            return null;

        int[] roomNumbers = IvVecMathHelper.add(mazeRooms.boundsHigher(), new int[]{1, 1, 1});

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

        return MazeGeneratorWithComponents.generatePaths(random, maze, transformedComponents);
    }

    @Override
    public void writeSyncedNBT(NBTTagCompound compound)
    {
        compound.setString("mazeID", mazeID);

        NBTTagCompound rooms = new NBTTagCompound();
        mazeRooms.writeToNBT(rooms);
        compound.setTag("rooms", rooms);

        NBTTagList exitsList = new NBTTagList();
        for (MazePath exit : mazeExits)
            exitsList.appendTag(exit.writeToNBT());
        compound.setTag("mazeExits", exitsList);

        BlockCoord.writeCoordToNBT("structureShift", structureShift, compound);

        compound.setIntArray("roomSize", roomSize);
    }

    @Override
    public void readSyncedNBT(NBTTagCompound compound)
    {
        mazeID = compound.getString("mazeID");

        NBTTagCompound rooms = compound.getCompoundTag("rooms");
        mazeRooms.readFromNBT(rooms, 3);

        // Legacy
        if (compound.hasKey("roomNumbers", Constants.NBT.TAG_INT_ARRAY))
            mazeRooms.add(new Selection.Area(true, new int[]{0, 0, 0}, IvVecMathHelper.sub(IvNBTHelper.readIntArrayFixedSize("roomNumbers", 3, compound), new int[]{1, 1, 1})));
        if (compound.hasKey("blockedRoomAreas", Constants.NBT.TAG_LIST))
        {
            NBTTagList blockedRoomsList = compound.getTagList("blockedRoomAreas", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < blockedRoomsList.tagCount(); i++)
            {
                NBTTagCompound blockedRoomTag = blockedRoomsList.getCompoundTagAt(i);
                mazeRooms.add(new Selection.Area(false, IvNBTHelper.readIntArrayFixedSize("min", 3, blockedRoomTag), IvNBTHelper.readIntArrayFixedSize("max", 3, blockedRoomTag)));
            }
        }

        mazeExits.clear();
        NBTTagList exitsList = compound.getTagList("mazeExits", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < exitsList.tagCount(); i++)
            mazeExits.add(new MazePath(exitsList.getCompoundTagAt(i)));

        structureShift = BlockCoord.readCoordFromNBT("structureShift", compound);

        roomSize = IvNBTHelper.readIntArrayFixedSize("roomSize", 3, compound);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void openEditGUI()
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditMazeBlock(this));
    }
}
