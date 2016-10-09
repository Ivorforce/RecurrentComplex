/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.maze;

import com.google.common.collect.*;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.utils.StructureBoundingBoxes;
import ivorius.reccomplex.utils.Transforms;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.maze.components.*;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.world.gen.feature.structure.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.MazeGenerationInfo;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.world.gen.structure.StructureBoundingBox;
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
    public static boolean generate(StructureSpawnContext context, PlacedStructure placedComponent)
    {
        if (!StructureRegistry.INSTANCE.has(placedComponent.structureID))
        {
            RecurrentComplex.logger.error(String.format("Could not find structure '%s' for maze", placedComponent.structureID));
            return false;
        }

        return new StructureGenerator<>().structureID(placedComponent.structureID).asChild(context).generationInfo(placedComponent.generationInfoID)
                .lowerCoord(placedComponent.lowerCoord).transform(placedComponent.transform)
                .instanceData(placedComponent.instanceData).generate().isPresent();
    }

    @Nullable
    public static PlacedStructure place(Random random, Environment environment, BlockPos coord, BlockPos shift, int[] roomSize, AxisAlignedTransform2D mazeTransform, ShiftedMazeComponent<MazeComponentStructure<Connector>, Connector> placedComponent)
    {
        MazeComponentStructure<Connector> componentInfo = placedComponent.getComponent();
        StructureInfo<?> structureInfo = StructureRegistry.INSTANCE.get(componentInfo.structureID);

        if (structureInfo == null)
        {
            RecurrentComplex.logger.error(String.format("Could not find structure '%s' for maze", componentInfo.structureID));
            return null;
        }

        AxisAlignedTransform2D componentTransform = componentInfo.transform.rotateClockwise(mazeTransform.getRotation());
        StructureBoundingBox compBoundingBox = getBoundingBox(coord, shift, roomSize, placedComponent, structureInfo, componentTransform, mazeTransform);
        NBTStorable instanceData = new StructureGenerator<>(structureInfo).random(random).environment(environment).transform(componentTransform).boundingBox(compBoundingBox).instanceData().orElse(null);

        return new PlacedStructure(componentInfo.structureID, componentInfo.structureID, componentTransform, StructureBoundingBoxes.min(compBoundingBox), instanceData);
    }

    protected static StructureBoundingBox getBoundingBox(BlockPos coord, BlockPos shift, int[] roomSize, ShiftedMazeComponent<MazeComponentStructure<Connector>, Connector> placedComponent, StructureInfo structureInfo, AxisAlignedTransform2D componentTransform, AxisAlignedTransform2D mazeTransform)
    {
        MazeRoom mazePosition = placedComponent.getShift();
        int[] scaledMazePosition = IvVecMathHelper.mul(mazePosition.getCoordinates(), roomSize);

        int[] structureBB = StructureInfos.structureSize(structureInfo, componentTransform);
        int[] compRoomSize = StructureInfos.structureSize(IvVecMathHelper.mul(placedComponent.getComponent().getSize(), roomSize), mazeTransform);
        int[] sizeDependentShift = new int[compRoomSize.length];
        for (int i = 0; i < structureBB.length; i++)
            sizeDependentShift[i] = (compRoomSize[i] - structureBB[i]) / 2;

        BlockPos lowerCoord = StructureInfos.transformedLowerCoord(
                coord.add(mazeTransform.apply(shift.add(scaledMazePosition[0], scaledMazePosition[1], scaledMazePosition[2]), new int[]{1, 1, 1}))
                        .add(sizeDependentShift[0], sizeDependentShift[1], sizeDependentShift[2]),
                structureBB, mazeTransform);

        return StructureInfos.structureBoundingBox(lowerCoord, structureBB);
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
