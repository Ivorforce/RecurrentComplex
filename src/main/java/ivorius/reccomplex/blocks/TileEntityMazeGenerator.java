/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import com.google.common.collect.*;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gnu.trove.list.array.TIntArrayList;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.maze.components.*;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import ivorius.ivtoolkit.tools.Visitor;
import ivorius.reccomplex.gui.editmazeblock.GuiEditMazeBlock;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.Selection;
import ivorius.reccomplex.structures.generic.maze.*;
import ivorius.reccomplex.utils.IntAreas;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.Constants;

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
        instanceData.placedStructures.addAll(WorldGenMaze.convertToPlacedStructures(context.random, getCoordinate(), structureShift, getPlacedRooms(context.random, context.transform), roomSize, context.transform));
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

    protected BlockCoord getCoordinate()
    {
        return new BlockCoord(xCoord, yCoord, zCoord);
    }

    @Override
    public boolean shouldPlaceInWorld(StructureSpawnContext context, InstanceData instanceData)
    {
        return false;
    }

    public List<ShiftedMazeComponent<MazeComponentStructure<Connector>, Connector>> getPlacedRooms(Random random, AxisAlignedTransform2D transform)
    {
        if (mazeRooms.isEmpty())
            return null;

        Connector roomConnector = new SimpleConnectors.Hermaphrodite("Path");
        Connector wallConnector = new SimpleConnectors.Hermaphrodite("Wall");

        int[] boundsHigher = mazeRooms.boundsHigher();
        int[] boundsLower = mazeRooms.boundsLower();

        int[] oneArray = new int[boundsHigher.length];
        Arrays.fill(oneArray, 1);

        final int[] outsideBoundsHigher = IvVecMathHelper.add(boundsHigher, oneArray);
        final int[] outsideBoundsLower = IvVecMathHelper.sub(boundsLower, oneArray);

        List<MazeComponentStructure<Connector>> transformedComponents = WorldGenMaze.transformedComponents(StructureRegistry.getStructuresInMaze(mazeID), roomConnector, wallConnector, transform);

        MorphingMazeComponent<Connector> maze = new SetMazeComponent<>();

        enclose(maze, new MazeRoom(outsideBoundsLower), new MazeRoom(outsideBoundsHigher), wallConnector);
        blockRooms(maze, mazeRooms.mazeRooms(false), wallConnector);
        addExits(roomConnector, maze, mazeExits);
        addRandomPaths(random, outsideBoundsHigher, maze, transformedComponents, roomConnector, outsideBoundsHigher[0] * outsideBoundsHigher[1] * outsideBoundsHigher[2] / (5 * 5 * 5) + 1);

        LimitAABBStrategy<MazeComponentStructure<Connector>, Connector> placementStrategy = new LimitAABBStrategy<>(outsideBoundsHigher, Collections.singleton(wallConnector));
        ConnectorStrategy connectionStrategy = new ConnectorStrategy();

        return MazeComponentConnector.randomlyConnect(maze, transformedComponents, connectionStrategy, placementStrategy, random);
    }

    protected static <C> void addRandomPaths(Random random, int[] size, MorphingMazeComponent<C> maze, List<? extends MazeComponent<C>> components, C roomConnector, int number)
    {
        Map<MazeRoomConnection, C> exits = new HashMap<>();
        for (MazeComponent<C> component : components)
            for (Map.Entry<MazeRoomConnection, C> entry : component.exits().entrySet())
                exits.put(entry.getKey(), entry.getValue());

        for (int i = 0; i < number; i++)
        {
            int[] randomCoords = new int[size.length];
            for (int c = 0; c < randomCoords.length; c++)
                randomCoords[c] = MathHelper.getRandomIntegerInRange(random, 0, size[c]);
            MazeRoom randomRoom = new MazeRoom(randomCoords);
            MazeRoomConnection randomConnection = new MazeRoomConnection(randomRoom, randomRoom.addInDimension(random.nextInt(size.length), random.nextBoolean() ? 1 : -1));
            if (Objects.equals(exits.get(randomConnection), roomConnector))
                maze.exits().put(randomConnection, roomConnector);
        }
    }

    protected static void addExits(Connector roomConnector, MorphingMazeComponent<Connector> maze, List<SavedMazePath> mazeExits)
    {
        Map<MazeRoomConnection, Connector> exitMap = Maps.newHashMap();
        SavedMazePaths.putAll(exitMap, Iterables.transform(mazeExits, SavedMazePaths.toConnectionFunction(roomConnector)));
        maze.exits().putAll(exitMap);
    }

    public static <C> void blockRooms(MorphingMazeComponent<C> component, Set<MazeRoom> rooms, C wallConnector)
    {
        component.add(WorldGenMaze.createCompleteComponent(rooms, Collections.<MazeRoomConnection, C>emptyMap(), wallConnector));
    }

    public static <C> void enclose(MorphingMazeComponent<C> component, MazeRoom lower, MazeRoom higher, C wallConnector)
    {
        if (lower.getDimensions() != higher.getDimensions())
            throw new IllegalArgumentException();

        final Set<MazeRoom> rooms = new HashSet<>();
        int[] coords = lower.getCoordinates();
        for (int i = 0; i < coords.length; i++)
        {
            final int lowest = lower.getCoordinate(i);
            final int highest = higher.getCoordinate(i);

            final int finalI = i;
            IntAreas.visitCoordsExcept(lower.getCoordinates(), higher.getCoordinates(), TIntArrayList.wrap(new int[]{i}), new Visitor<int[]>()
            {
                @Override
                public boolean visit(int[] ints)
                {
                    ints[finalI] = lowest;
                    rooms.add(new MazeRoom(ints));
                    ints[finalI] = highest;
                    rooms.add(new MazeRoom(ints));

                    return true;
                }
            });
        }

        blockRooms(component, rooms, wallConnector);
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
        public final List<PlacedStructure> placedStructures = new ArrayList<>();

        public InstanceData()
        {

        }

        public InstanceData(NBTTagCompound compound)
        {
            placedStructures.addAll(NBTCompoundObjects.readListFrom(compound, "placedStructures", PlacedStructure.class));
        }

        @Override
        public NBTBase writeToNBT()
        {
            NBTTagCompound compound = new NBTTagCompound();
            NBTCompoundObjects.writeListTo(compound, "placedStructures", placedStructures);
            return compound;
        }
    }
}
