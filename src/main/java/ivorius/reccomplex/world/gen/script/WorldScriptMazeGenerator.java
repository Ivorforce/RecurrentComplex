/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.script;

import gnu.trove.list.array.TIntArrayList;
import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.maze.components.*;
import ivorius.ivtoolkit.tools.IvNBTHelper;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import ivorius.ivtoolkit.tools.NBTTagLists;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.TableDataSourceWorldScriptMazeGenerator;
import ivorius.reccomplex.utils.IntAreas;
import ivorius.reccomplex.utils.NBTStorable;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.Selection;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules.BlockedConnectorStrategy;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules.LimitAABBStrategy;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules.MazeRule;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.rules.MazeRuleRegistry;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 13.09.15.
 */
public class WorldScriptMazeGenerator implements WorldScript<WorldScriptMazeGenerator.InstanceData>
{
    public final List<MazeRule> rules = new ArrayList<>();
    public String mazeID = "";
    public BlockPos structureShift = BlockPos.ORIGIN;
    public int[] roomSize = new int[]{3, 5, 3};
    public SavedMazeComponent mazeComponent = new SavedMazeComponent();

    public static <C> void blockRooms(MorphingMazeComponent<C> component, Set<MazeRoom> rooms, C wallConnector)
    {
        component.add(WorldGenMaze.createCompleteComponent(rooms, Collections.emptyMap(), wallConnector));
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
            IntAreas.visitCoordsExcept(lower.getCoordinates(), higher.getCoordinates(), TIntArrayList.wrap(new int[]{i}), ints ->
            {
                ints[finalI] = lowest;
                rooms.add(new MazeRoom(ints));
                ints[finalI] = highest;
                rooms.add(new MazeRoom(ints));

                return true;
            });
        }

        blockRooms(component, rooms, wallConnector);
    }

    @Override
    public void generate(StructureSpawnContext context, InstanceData instanceData, BlockPos pos)
    {
        for (PlacedStructure placedComponent : instanceData.placedStructures)
            WorldGenMaze.generate(context, placedComponent, pos);
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

    public BlockPos getStructureShift()
    {
        return structureShift;
    }

    public void setStructureShift(BlockPos structureShift)
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

    public SavedMazeComponent getMazeComponent()
    {
        return mazeComponent;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        mazeID = compound.getString("mazeID");

        if (compound.hasKey("mazeComponent", Constants.NBT.TAG_COMPOUND))
            mazeComponent.readFromNBT(compound.getCompoundTag("mazeComponent"));
        else // Legacy
        {
            NBTTagCompound rooms = compound.getCompoundTag("rooms");
            mazeComponent.rooms.readFromNBT(rooms);

            // Legacy
            if (compound.hasKey("roomNumbers", Constants.NBT.TAG_INT_ARRAY))
                mazeComponent.rooms.add(new Selection.Area(true, new int[]{0, 0, 0}, IvVecMathHelper.sub(IvNBTHelper.readIntArrayFixedSize("roomNumbers", 3, compound), new int[]{1, 1, 1})));
            if (compound.hasKey("blockedRoomAreas", Constants.NBT.TAG_LIST))
            {
                NBTTagList blockedRoomsList = compound.getTagList("blockedRoomAreas", Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < blockedRoomsList.tagCount(); i++)
                {
                    NBTTagCompound blockedRoomTag = blockedRoomsList.getCompoundTagAt(i);
                    mazeComponent.rooms.add(new Selection.Area(false, IvNBTHelper.readIntArrayFixedSize("min", 3, blockedRoomTag), IvNBTHelper.readIntArrayFixedSize("max", 3, blockedRoomTag)));
                }
            }

            mazeComponent.exitPaths.clear();
            mazeComponent.exitPaths.addAll(NBTCompoundObjects.readListFrom(compound, "mazeExits", SavedMazePathConnection::new));

            mazeComponent.defaultConnector.id = ConnectorStrategy.DEFAULT_WALL;
            mazeComponent.reachability.set(Collections.emptyList(), Collections.emptyList());
        }

        structureShift = BlockPositions.readFromNBT("structureShift", compound);

        roomSize = IvNBTHelper.readIntArrayFixedSize("roomSize", 3, compound);

        rules.clear();
        rules.addAll(NBTTagLists.compoundsFrom(compound, "rules").stream().map(MazeRuleRegistry.INSTANCE::read).collect(Collectors.toList()));
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setString("mazeID", mazeID);

        compound.setTag("mazeComponent", NBTCompoundObjects.write(mazeComponent));

        BlockPositions.writeToNBT("structureShift", structureShift, compound);

        compound.setIntArray("roomSize", roomSize);

        NBTTagLists.writeTo(compound, "rules", rules.stream().map(MazeRuleRegistry.INSTANCE::write).collect(Collectors.toList()));
    }

    @Override
    public InstanceData prepareInstanceData(StructurePrepareContext context, BlockPos pos)
    {
        InstanceData instanceData = new InstanceData();

        instanceData.placedStructures.addAll(getPlacedRooms(context.random, context.transform, context.environment).stream()
                .map(placedComponent -> WorldGenMaze.place(context.random, context.environment, structureShift, roomSize, placedComponent, pos, context.transform))
                .filter(Objects::nonNull).collect(Collectors.toList()));

        return instanceData;
    }

    @Override
    public InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return new InstanceData(nbt instanceof NBTTagCompound ? (NBTTagCompound) nbt : new NBTTagCompound());
    }

    public List<PlacedMazeComponent<MazeComponentStructure<Connector>, Connector>> getPlacedRooms(Random random, AxisAlignedTransform2D transform, Environment environment)
    {
        if (mazeComponent.rooms.isEmpty())
            return null;

        ConnectorFactory factory = new ConnectorFactory();

        Connector wallConnector = factory.get(ConnectorStrategy.DEFAULT_WALL);
        Connector defaultConnector = mazeComponent.defaultConnector.toConnector(factory);
        Set<Connector> blockedConnections = Collections.singleton(wallConnector); // TODO Make configurable

        int[] boundsHigher = mazeComponent.rooms.boundsHigher();
        int[] boundsLower = mazeComponent.rooms.boundsLower();

        int[] oneArray = new int[boundsHigher.length];
        Arrays.fill(oneArray, 1);

        final int[] outsideBoundsHigher = IvVecMathHelper.add(boundsHigher, oneArray);
        final int[] outsideBoundsLower = IvVecMathHelper.sub(boundsLower, oneArray);

        List<MazeComponentStructure<Connector>> transformedComponents = StructureRegistry.INSTANCE.getStructuresInMaze(mazeID)
                .flatMap(pair -> pair.getLeft().declaredVariables().omega(environment, true)
                        .flatMap(domain -> WorldGenMaze.transforms(pair.getLeft(), pair.getRight(), transform, factory, environment.copy(domain), blockedConnections))
                )
                .collect(Collectors.toList());

        MorphingMazeComponent<Connector> maze = new SetMazeComponent<>();

        WorldScriptMazeGenerator.enclose(maze, new MazeRoom(outsideBoundsLower), new MazeRoom(outsideBoundsHigher), defaultConnector);
        WorldScriptMazeGenerator.blockRooms(maze, mazeComponent.rooms.compile(false).keySet(), defaultConnector);

        WorldGenMaze.buildExitPaths(environment, factory, mazeComponent.exitPaths, maze.rooms()).forEach(path -> maze.exits().put(path.getKey(), path.getValue()));

        // Don't add reachability as it only slows down generation without adding real features
//        maze.reachability().putAll(mazeComponent.reachability.build(AxisAlignedTransform2D.ORIGINAL, mazeComponent.boundsSize(), SavedMazeReachability.notBlocked(blockedConnections, maze.exits()), maze.exits().keySet()));

        ConnectorStrategy connectorStrategy = new ConnectorStrategy();

        List<MazePredicate<Connector>> predicates = rules.stream().map(r -> r.build(this, blockedConnections, factory, transformedComponents, connectorStrategy)).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
        predicates.add(new LimitAABBStrategy<>(outsideBoundsHigher));
        predicates.add(new BlockedConnectorStrategy<>(blockedConnections));

        int totalRooms = mazeComponent.rooms.compile(true).size();

        return MazeComponentConnector.connect(maze,
                transformedComponents, connectorStrategy,
                new MazePredicateMany<>(predicates),
                random,
                RCConfig.mazePlacementReversesPerRoom >= 0 ? MathHelper.floor(totalRooms * RCConfig.mazePlacementReversesPerRoom + 0.5f) : MazeComponentConnector.INFINITE_REVERSES
        );
    }

    public static class InstanceData implements NBTStorable
    {
        public final List<PlacedStructure> placedStructures = new ArrayList<>();

        public InstanceData()
        {

        }

        public InstanceData(NBTTagCompound compound)
        {
            placedStructures.addAll(NBTCompoundObjects.readListFrom(compound, "placedStructures", PlacedStructure::new));
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
