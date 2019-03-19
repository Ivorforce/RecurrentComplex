/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.maze;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.math.Transforms;
import ivorius.ivtoolkit.maze.components.*;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.nbt.NBTStorable;
import ivorius.reccomplex.utils.RCAxisAlignedTransform;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.MazeGeneration;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by lukas on 27.06.14.
 */
public class WorldGenMaze
{
    public static Boolean generate(StructureSpawnContext context, PlacedStructure placedComponent, BlockPos pos)
    {
        Structure<?> structure = StructureRegistry.INSTANCE.get(placedComponent.structureID);

        if (structure == null)
        {
            RecurrentComplex.logger.error(String.format("Could not find structure '%s' for maze", placedComponent.structureID));
            return false;
        }

        StructureGenerator<?> generator = new StructureGenerator<>(structure).asChild(context).generationInfo(placedComponent.generationInfoID)
                .transform(Transforms.apply(placedComponent.transform, context.transform))
                .lowerCoord(lowerCoord(structure, placedComponent.lowerCoord, placedComponent.transform, pos, context.transform))
                .structureID(placedComponent.structureID)
                .instanceData(placedComponent.instanceData);

        // Never abort on first time to get the entry added
        if (!context.generateMaturity.isFirstTime() && context.generationBB != null && !context.generationBB.intersectsWith(generator.boundingBox().get()))
            return null;

        return generator.generate().succeeded();
    }

    protected static BlockPos lowerCoord(Structure structure, BlockPos lowerCoord, AxisAlignedTransform2D placedTransform, BlockPos pos, AxisAlignedTransform2D transform)
    {
        int[] placedSize = RCAxisAlignedTransform.applySize(placedTransform, structure.size());
        return transform.apply(lowerCoord, IvVecMathHelper.sub(new int[]{2, 2, 2}, placedSize)).add(pos);
    }

    @Nullable
    public static PlacedStructure place(Random random, Environment environment, BlockPos shift, int[] roomSize, PlacedMazeComponent<MazeComponentStructure<Connector>, Connector> placedComponent, BlockPos pos, AxisAlignedTransform2D transform)
    {
        MazeComponentStructure<Connector> componentInfo = placedComponent.component();
        Structure<?> structure = StructureRegistry.INSTANCE.get(componentInfo.structureID);

        if (structure == null)
        {
            RecurrentComplex.logger.error(String.format("Could not find structure '%s' for maze", componentInfo.structureID));
            return null;
        }

        // Add the component's variables first (precedence, these were set already!) and then add the environment's vars
        Environment childEnvironment = environment.copy(componentInfo.variableDomain);
        environment.variables.fill(childEnvironment.variables);

        BlockPos compLowerPos = getBoundingBox(roomSize, placedComponent, structure, componentInfo.transform).add(shift);

        NBTStorable instanceData = new StructureGenerator<>(structure).seed(random.nextLong()).environment(childEnvironment)
                .transform(Transforms.apply(componentInfo.transform, transform))
                .lowerCoord(lowerCoord(structure, compLowerPos, componentInfo.transform, pos, transform))
                .structureID(componentInfo.structureID)
                .instanceData().orElse(null);
        return new PlacedStructure(componentInfo.structureID, componentInfo.structureID, componentInfo.transform, compLowerPos, instanceData.writeToNBT());
    }

    protected static BlockPos getBoundingBox(int[] roomSize, PlacedMazeComponent<MazeComponentStructure<Connector>, Connector> placedComponent, Structure structure, AxisAlignedTransform2D transform)
    {
        int[] scaledMazePosition = IvVecMathHelper.mul(placedComponent.shift().getCoordinates(), roomSize);

        int[] size = RCAxisAlignedTransform.applySize(transform, structure.size());
        int[] expectedSize = IvVecMathHelper.mul(placedComponent.component().getSize(), roomSize); // Already rotated component, so don't rotate again
        int[] sizeDependentShift = new int[expectedSize.length];
        for (int i = 0; i < size.length; i++)
            sizeDependentShift[i] = (expectedSize[i] - size[i]) / 2;

        return new BlockPos(scaledMazePosition[0], scaledMazePosition[1], scaledMazePosition[2])
                .add(sizeDependentShift[0], sizeDependentShift[1], sizeDependentShift[2]);

    }

    public static Stream<MazeComponentStructure<Connector>> transforms(Structure info, MazeGeneration mazeInfo, AxisAlignedTransform2D transform, ConnectorFactory factory, Environment environment, Collection<Connector> blockedConnections)
    {
        int[] compSize = mazeInfo.mazeComponent.boundsSize();

        List<AxisAlignedTransform2D> transforms = Transforms.transformStream(r -> info.isRotatable() || transform.apply(r) == 0, m -> info.isMirrorable() || m == 0).collect(Collectors.toList());

        double compWeight = mazeInfo.getWeight() * info.declaredVariables().chance(environment)
                * RCConfig.tweakedSpawnRate(StructureRegistry.INSTANCE.id(info));
        double splitCompWeight = compWeight / transforms.size();

        return transforms.stream().map(t -> transform(info, mazeInfo.mazeComponent, t, compSize, splitCompWeight, factory, environment, blockedConnections));
    }

    public static MazeComponentStructure<Connector> transform(Structure info, SavedMazeComponent comp, final AxisAlignedTransform2D transform, final int[] size, double weight, ConnectorFactory factory, Environment environment, Collection<Connector> blockedConnections)
    {
        Collection<MazeRoom> rooms = comp.getRooms();
        Set<MazeRoom> transformedRooms = rooms.stream().map(input -> MazeRooms.rotated(input, transform, size)).collect(Collectors.toSet());

        Map<MazePassage, Connector> transformedExits = new HashMap<>();
        buildExitPaths(environment, factory, comp.getExitPaths(), rooms).forEach(path -> transformedExits.put(MazePassages.rotated(path.getKey(), transform, size), path.getValue()));
        addMissingExits(transformedRooms, transformedExits, comp.defaultConnector.toConnector(factory));

        ImmutableMultimap.Builder<MazePassage, MazePassage> reachability = ImmutableMultimap.builder();
        comp.reachability.build(reachability, transform, size, SavedMazeReachability.notBlocked(blockedConnections, transformedExits), transformedExits.keySet());
        addExternalReachability(reachability, transformedExits, blockedConnections);

        return new MazeComponentStructure<>(weight, StructureRegistry.INSTANCE.id(info), environment.variables, transform, ImmutableSet.copyOf(transformedRooms), ImmutableMap.copyOf(transformedExits), reachability.build());
    }

    public static ImmutableMultimap.Builder<MazePassage, MazePassage> addExternalReachability(ImmutableMultimap.Builder<MazePassage, MazePassage> reachability, Map<MazePassage, Connector> transformedExits, Collection<Connector> blockedConnections)
    {
        transformedExits.keySet().stream()
                .filter(passage -> !blockedConnections.contains(transformedExits.get(passage)))
                .forEach(passage -> reachability.put(passage, passage.inverse()));
        return reachability;
    }

    public static <C> SetMazeComponent<C> createCompleteComponent(Set<MazeRoom> rooms, Map<MazePassage, C> exits, C wallConnector)
    {
        SetMazeComponent<C> component = new SetMazeComponent<>(rooms, exits, HashMultimap.create());
        addMissingExits(component.rooms, component.exits, wallConnector);
        return component;
    }

    public static <C> void addMissingExits(Set<MazeRoom> rooms, Map<MazePassage, C> exits, C connector)
    {
        for (MazeRoom room : rooms)
            MazeRooms.neighborPassages(room).filter(passage -> !exits.containsKey(passage) && !(rooms.contains(passage.getLeft()) && rooms.contains(passage.getRight()))).forEach(passage -> exits.put(passage, connector));
    }

    public static Stream<Map.Entry<MazePassage, Connector>> buildExitPaths(Environment environment, ConnectorFactory factory, List<SavedMazePathConnection> mazeExits, Collection<MazeRoom> rooms)
    {
        return mazeExits.stream().map(SavedMazePaths.buildFunction(environment, factory)) // Build
                .map(e -> rooms.contains(e.getKey().getSource()) ? e : Pair.of(e.getKey().inverse(), e.getValue())); // Inverse wrongly directed paths
    }
}
