/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.scripts.world;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import gnu.trove.list.array.TIntArrayList;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.maze.components.*;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import ivorius.ivtoolkit.tools.Visitor;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.TableDataSourceWorldScriptMazeGenerator;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.Selection;
import ivorius.reccomplex.structures.generic.maze.*;
import ivorius.reccomplex.utils.IntAreas;
import ivorius.reccomplex.utils.IvTranslations;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.*;

/**
 * Created by lukas on 13.09.15.
 */
public class WorldScriptMazeGenerator implements WorldScript<WorldScriptMazeGenerator.InstanceData>
{
    public String mazeID = "";
    public List<SavedMazePathConnection> mazeExits = new ArrayList<>();
    public Selection mazeRooms = Selection.zeroSelection(3);
    public BlockCoord structureShift = new BlockCoord(0, 0, 0);
    public int[] roomSize = new int[]{3, 5, 3};

    public static <C> void addRandomPaths(Random random, int[] size, MorphingMazeComponent<C> maze, List<? extends MazeComponent<C>> components, C roomConnector, int number)
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

    public static void addExits(ConnectorFactory factory, MorphingMazeComponent<Connector> maze, List<SavedMazePathConnection> mazeExits)
    {
        SavedMazePaths.putAll(maze.exits(), Iterables.transform(mazeExits, SavedMazePaths.toConnectionFunction(factory)));
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
    public void generate(StructureSpawnContext context, InstanceData instanceData, BlockCoord coord)
    {
        List<PlacedStructure> placedStructures = instanceData.placedStructures;
        if (placedStructures == null)
            return;
        WorldGenMaze.generatePlacedStructures(placedStructures, context);
    }

    @Override
    public String getDisplayString()
    {
        return IvTranslations.get("reccomplex.worldscript.mazeGen");
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate tableDelegate)
    {
        return new TableDataSourceWorldScriptMazeGenerator(this, tableDelegate, navigator);
    }

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
        mazeExits.addAll(NBTCompoundObjects.readListFrom(nbtTagCompound, "mazeExits", SavedMazePathConnection.class));

        structureShift = BlockCoord.readCoordFromNBT("structureShift", nbtTagCompound);

        roomSize = IvNBTHelper.readIntArrayFixedSize("roomSize", 3, nbtTagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        nbtTagCompound.setString("mazeID", mazeID);

        NBTTagCompound rooms = new NBTTagCompound();
        mazeRooms.writeToNBT(rooms);
        nbtTagCompound.setTag("rooms", rooms);

        NBTCompoundObjects.writeListTo(nbtTagCompound, "mazeExits", mazeExits);

        BlockCoord.writeCoordToNBT("structureShift", structureShift, nbtTagCompound);

        nbtTagCompound.setIntArray("roomSize", roomSize);
    }

    @Override
    public WorldScriptMazeGenerator.InstanceData prepareInstanceData(StructurePrepareContext context, BlockCoord coord, World world)
    {
        WorldScriptMazeGenerator.InstanceData instanceData = new WorldScriptMazeGenerator.InstanceData();
        instanceData.placedStructures.addAll(WorldGenMaze.convertToPlacedStructures(context.random, coord, structureShift, getPlacedRooms(context.random, context.transform), roomSize, context.transform));
        return instanceData;
    }

    @Override
    public WorldScriptMazeGenerator.InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return new WorldScriptMazeGenerator.InstanceData(nbt instanceof NBTTagCompound ? (NBTTagCompound) nbt : new NBTTagCompound());
    }

    public List<ShiftedMazeComponent<MazeComponentStructure<Connector>, Connector>> getPlacedRooms(Random random, AxisAlignedTransform2D transform)
    {
        if (mazeRooms.isEmpty())
            return null;

        ConnectorFactory factory = new ConnectorFactory();

        Connector roomConnector = factory.get("Path");
        Connector wallConnector = factory.get("Wall");
        Set<Connector> blockedConnections = Collections.singleton(wallConnector); // TODO Make configurable

        int[] boundsHigher = mazeRooms.boundsHigher();
        int[] boundsLower = mazeRooms.boundsLower();

        int[] oneArray = new int[boundsHigher.length];
        Arrays.fill(oneArray, 1);

        final int[] outsideBoundsHigher = IvVecMathHelper.add(boundsHigher, oneArray);
        final int[] outsideBoundsLower = IvVecMathHelper.sub(boundsLower, oneArray);

        List<MazeComponentStructure<Connector>> transformedComponents = WorldGenMaze.transformedComponents(StructureRegistry.INSTANCE.getStructuresInMaze(mazeID), factory, transform, blockedConnections);

        MorphingMazeComponent<Connector> maze = new SetMazeComponent<>();

        WorldScriptMazeGenerator.enclose(maze, new MazeRoom(outsideBoundsLower), new MazeRoom(outsideBoundsHigher), wallConnector);
        WorldScriptMazeGenerator.blockRooms(maze, mazeRooms.mazeRooms(false), wallConnector);
        WorldScriptMazeGenerator.addExits(factory, maze, mazeExits);
        WorldScriptMazeGenerator.addRandomPaths(random, outsideBoundsHigher, maze, transformedComponents, roomConnector, outsideBoundsHigher[0] * outsideBoundsHigher[1] * outsideBoundsHigher[2] / (5 * 5 * 5) + 1);

        MazePredicate<MazeComponentStructure<Connector>, Connector> placementStrategy = new MazePredicateMany<>(
                new LimitAABBStrategy<MazeComponentStructure<Connector>, Connector>(outsideBoundsHigher),
                new BlockedConnectorStrategy<MazeComponentStructure<Connector>, Connector>(blockedConnections)
        );

        ConnectorStrategy connectionStrategy = new ConnectorStrategy();

        int totalRooms = 1;
        for (int i = 0; i < outsideBoundsLower.length; i++)
            totalRooms *= outsideBoundsHigher[i] - outsideBoundsLower[i] + 1;

        return MazeComponentConnector.randomlyConnect(maze, transformedComponents, connectionStrategy, placementStrategy, random, totalRooms * 10);
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
