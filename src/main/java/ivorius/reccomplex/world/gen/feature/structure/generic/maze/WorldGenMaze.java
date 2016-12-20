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
import ivorius.ivtoolkit.maze.components.*;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.utils.NBTStorable;
import ivorius.ivtoolkit.math.Transforms;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import ivorius.reccomplex.world.gen.feature.structure.*;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.MazeGenerationInfo;
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
    public static boolean generate(StructureSpawnContext context, PlacedStructure placedComponent, BlockPos pos)
    {
        StructureInfo<?> structure = StructureRegistry.INSTANCE.get(placedComponent.structureID);

        if (structure == null)
        {
            RecurrentComplex.logger.error(String.format("Could not find structure '%s' for maze", placedComponent.structureID));
            return false;
        }

        int[] placedSize = StructureInfos.structureSize(structure, placedComponent.transform);
        return new StructureGenerator<>(structure).asChild(context).generationInfo(placedComponent.generationInfoID)
                .lowerCoord(context.transform.apply(placedComponent.lowerCoord, IvVecMathHelper.sub(new int[]{2, 2, 2}, placedSize)).add(pos))
                .transform(Transforms.apply(placedComponent.transform, context.transform)).structureID(placedComponent.structureID)
                .instanceData(placedComponent.instanceData).generate().isPresent();
    }

    @Nullable
    public static PlacedStructure place(Random random, Environment environment, BlockPos shift, int[] roomSize, ShiftedMazeComponent<MazeComponentStructure<Connector>, Connector> placedComponent)
    {
        MazeComponentStructure<Connector> componentInfo = placedComponent.getComponent();
        StructureInfo<?> structureInfo = StructureRegistry.INSTANCE.get(componentInfo.structureID);

        if (structureInfo == null)
        {
            RecurrentComplex.logger.error(String.format("Could not find structure '%s' for maze", componentInfo.structureID));
            return null;
        }

        BlockPos compLowerPos = getBoundingBox(roomSize, placedComponent, structureInfo, componentInfo.transform).add(shift);
        NBTStorable instanceData = new StructureGenerator<>(structureInfo).random(random).environment(environment).transform(componentInfo.transform).lowerCoord(compLowerPos).instanceData().orElse(null);

        return new PlacedStructure(componentInfo.structureID, componentInfo.structureID, componentInfo.transform, compLowerPos, instanceData.writeToNBT());
    }

    protected static BlockPos getBoundingBox(int[] roomSize, ShiftedMazeComponent<MazeComponentStructure<Connector>, Connector> placedComponent, StructureInfo structureInfo, AxisAlignedTransform2D transform)
    {
        int[] scaledMazePosition = IvVecMathHelper.mul(placedComponent.getShift().getCoordinates(), roomSize);

        int[] size = StructureInfos.structureSize(structureInfo, transform);
        int[] expectedSize = IvVecMathHelper.mul(placedComponent.getComponent().getSize(), roomSize); // Already rotated component, so don't rotate again
        int[] sizeDependentShift = new int[expectedSize.length];
        for (int i = 0; i < size.length; i++)
            sizeDependentShift[i] = (expectedSize[i] - size[i]) / 2;

        return new BlockPos(scaledMazePosition[0], scaledMazePosition[1], scaledMazePosition[2])
                .add(sizeDependentShift[0], sizeDependentShift[1], sizeDependentShift[2]);

    }

    public static Stream<MazeComponentStructure<Connector>> transforms(StructureInfo info, MazeGenerationInfo mazeInfo, ConnectorFactory factory, AxisAlignedTransform2D transform, Collection<Connector> blockedConnections)
    {
        int[] compSize = mazeInfo.mazeComponent.boundsSize();

        List<AxisAlignedTransform2D> transforms = Transforms.transformStream(r -> info.isRotatable() || transform.apply(r) == 0, m -> info.isMirrorable() || m == 0).collect(Collectors.toList());

        double compWeight = mazeInfo.getWeight() * RCConfig.tweakedSpawnRate(StructureRegistry.INSTANCE.id(info));
        double splitCompWeight = compWeight / transforms.size();

        return transforms.stream().map(t -> transform(info, mazeInfo.mazeComponent, t, compSize, splitCompWeight, factory, blockedConnections));
    }

    public static MazeComponentStructure<Connector> transform(StructureInfo info, SavedMazeComponent comp, final AxisAlignedTransform2D transform, final int[] size, double weight, ConnectorFactory factory, Collection<Connector> blockedConnections)
    {
        Set<MazeRoom> transformedRooms = comp.getRooms().stream().map(input -> MazeRooms.rotated(input, transform, size)).collect(Collectors.toSet());

        Map<MazePassage, Connector> transformedExits = new HashMap<>();
        buildExitPaths(factory, comp.getExitPaths(), transformedRooms).forEach(path -> transformedExits.put(MazePassages.rotated(path.getKey(), transform, size), path.getValue()));
        addMissingExits(transformedRooms, transformedExits, comp.defaultConnector.toConnector(factory));

        ImmutableMultimap<MazePassage, MazePassage> reachability = comp.reachability.build(transform, size, SavedMazeReachability.notBlocked(blockedConnections, transformedExits), transformedExits.keySet());

        return new MazeComponentStructure<>(weight, StructureRegistry.INSTANCE.id(info), transform, ImmutableSet.copyOf(transformedRooms), ImmutableMap.copyOf(transformedExits), reachability);
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

    public static Stream<Map.Entry<MazePassage, Connector>> buildExitPaths(ConnectorFactory factory, List<SavedMazePathConnection> mazeExits, Set<MazeRoom> rooms)
    {
        return mazeExits.stream().map(SavedMazePaths.buildFunction(factory)) // Build
                .map(e -> rooms.contains(e.getKey().getSource()) ? e : Pair.of(e.getKey().inverse(), e.getValue())); // Inverse wrongly directed paths
    }
}
