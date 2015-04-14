/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.maze.*;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import ivorius.ivtoolkit.tools.NBTTagCompounds;
import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.reccomplex.gui.editmazeblock.GuiEditMazeBlock;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.Selection;
import ivorius.reccomplex.structures.generic.maze.SavedMazePath;
import ivorius.reccomplex.structures.generic.maze.SavedMazePaths;
import ivorius.reccomplex.structures.generic.maze.WorldGenMaze;
import ivorius.reccomplex.structures.generic.maze.WorldGenMaze.PlacedStructure;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by lukas on 06.06.14.
 */
public class TileEntityMazeGenerator extends TileEntity implements GeneratingTileEntity<TileEntityMazeGenerator.InstanceData>, TileEntityWithGUI
{
    public String mazeID = "";
    public List<SavedMazePath> mazeExits = new ArrayList<>();
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
    public InstanceData prepareInstanceData(StructurePrepareContext context)
    {
        InstanceData instanceData = new InstanceData();
        instanceData.placedStructures = WorldGenMaze.convertToPlacedStructures(context.random, getLowerCoord(context.transform), getPlacedRooms(context), roomSize);
        return instanceData;
    }

    @Override
    public InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return new InstanceData(nbt instanceof NBTTagCompound ? (NBTTagCompound) nbt : new NBTTagCompound());
    }

    @Override
    public void generate(StructureSpawnContext context, InstanceData instanceData)
    {
       List<PlacedStructure> placedStructures = instanceData.placedStructures;
        if (placedStructures == null)
            return;
        WorldGenMaze.generatePlacedStructures(placedStructures, context);
    }

    protected BlockCoord getLowerCoord(AxisAlignedTransform2D transform)
    {
        int[] roomNumbers = IvVecMathHelper.add(mazeRooms.boundsHigher(), new int[]{1, 1, 1});
        int[] mazeSize = new int[]{roomSize[0] * roomNumbers[0], roomSize[1] * roomNumbers[1], roomSize[2] * roomNumbers[2]};
        return transform.apply(structureShift, new int[]{1, 1, 1}).add(xCoord, yCoord, zCoord).subtract(transform.apply(new BlockCoord(0, 0, 0), mazeSize));
    }

    @Override
    public boolean shouldPlaceInWorld(StructureSpawnContext context, InstanceData instanceData)
    {
        return false;
    }

    public List<MazeComponentPosition> getPlacedRooms(StructurePrepareContext context)
    {
        if (mazeRooms.isEmpty())
            return null;

        AxisAlignedTransform2D transform = context.transform;

        int[] roomNumbers = IvVecMathHelper.add(mazeRooms.boundsHigher(), new int[]{1, 1, 1});

        Maze maze = new Maze(roomNumbers[0] * 2 + 1, roomNumbers[1] * 2 + 1, roomNumbers[2] * 2 + 1);

        List<MazeComponent> transformedComponents = WorldGenMaze.transformedComponents(StructureRegistry.getStructuresInMaze(mazeID));
        Set<Integer> pathDims = new HashSet<>();
        for (MazeComponent mazeComponent : transformedComponents)
        {
            for (MazePath path : mazeComponent.getExitPaths())
                pathDims.add(path.getPathDimension());
        }

        Collection<MazeRoom> blockedRooms = mazeRooms.mazeRooms(false);

        MazeGenerator.generateStartPathsForEnclosedMaze(maze, Lists.transform(mazeExits, SavedMazePaths.toPathFunction()), blockedRooms, context.transform);
        for (int i = 0; i < roomNumbers[0] * roomNumbers[1] * roomNumbers[2] / (5 * 5 * 5) + 1; i++)
        {
            MazePath randPath = MazeGenerator.randomEmptyPathInMaze(context.random, maze, pathDims);
            if (randPath != null)
                maze.set(Maze.ROOM, randPath);
            else
                break;
        }

        return MazeGeneratorWithComponents.generatePaths(context.random, maze, transformedComponents);
    }

    @Override
    public void writeSyncedNBT(NBTTagCompound compound)
    {
        compound.setString("mazeID", mazeID);

        NBTTagCompound rooms = new NBTTagCompound();
        mazeRooms.writeToNBT(rooms);
        compound.setTag("rooms", rooms);

        NBTCompoundObjects.writeListTo(compound, "mazeExits", mazeExits);

        BlockCoord.writeCoordToNBT("structureShift", structureShift, compound);

        compound.setIntArray("roomSize", roomSize);
    }

    @Override
    public void readSyncedNBT(final NBTTagCompound compound)
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
        mazeExits.addAll(NBTCompoundObjects.readListFrom(compound, "mazeExits", SavedMazePath.class));

        structureShift = BlockCoord.readCoordFromNBT("structureShift", compound);

        roomSize = IvNBTHelper.readIntArrayFixedSize("roomSize", 3, compound);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void openEditGUI()
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditMazeBlock(this));
    }

    public static class InstanceData implements NBTStorable
    {
        public List<PlacedStructure> placedStructures;

        public InstanceData()
        {

        }

        public InstanceData(NBTTagCompound compound)
        {
            placedStructures = NBTTagCompounds.readFrom(compound, "placedStructures", PlacedStructure.class);
        }

        @Override
        public NBTBase writeToNBT()
        {
            NBTTagCompound compound = new NBTTagCompound();

            compound.setTag("placedStructures", NBTTagCompounds.write(placedStructures));

            return compound;
        }
    }
}
