/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import com.google.common.collect.*;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.maze.components.*;
import ivorius.ivtoolkit.tools.GuavaCollectors;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.structures.*;
import ivorius.reccomplex.structures.generic.gentypes.MazeGenerationInfo;
import ivorius.reccomplex.utils.NBTStorable;
import ivorius.reccomplex.worldgen.StructureGenerationData;
import ivorius.reccomplex.worldgen.StructureGenerator;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by lukas on 27.06.14.
 */
public class WorldGenMaze
{
    public static boolean generatePlacedStructures(List<PlacedStructure> placedStructures, StructureSpawnContext context)
    {
        for (PlacedStructure placedComponent : placedStructures)
        {
            StructureInfo structureInfo = StructureRegistry.INSTANCE.getStructure(placedComponent.structureID);

            if (structureInfo != null && placedComponent.instanceData != null)
            {
                AxisAlignedTransform2D componentTransform = placedComponent.transform;
                StructureBoundingBox compBoundingBox = StructureInfos.structureBoundingBox(placedComponent.lowerCoord, StructureInfos.structureSize(structureInfo, componentTransform));

                BlockCoord coord = new BlockCoord(compBoundingBox.minX, compBoundingBox.minY, compBoundingBox.minZ);

                StructureGenerator.partially(structureInfo, context.world, context.random, coord, componentTransform, context.generationBB, context.generationLayer + 1, placedComponent.structureID, placedComponent.instanceData, context.isFirstTime);

                if (context.isFirstTime)
                    StructureGenerationData.get(context.world).addCompleteEntry(placedComponent.structureID, coord, componentTransform);
            }
            else
            {
                RecurrentComplex.logger.error(String.format("Could not find structure '%s' for maze", placedComponent.structureID));
            }
        }

        return true;
    }

    public static List<PlacedStructure> convertToPlacedStructures(final Random random, final BlockCoord coord, final BlockCoord shift, List<ShiftedMazeComponent<MazeComponentStructure<Connector>, Connector>> placedComponents, final int[] roomSize, final AxisAlignedTransform2D mazeTransform)
    {
        return Lists.newArrayList(placedComponents.stream().map((Function<ShiftedMazeComponent<MazeComponentStructure<Connector>, Connector>, PlacedStructure>) placedComponent -> {
            MazeComponentStructure<Connector> componentInfo = placedComponent.getComponent();
            StructureInfo structureInfo = StructureRegistry.INSTANCE.getStructure(componentInfo.structureID);

            if (structureInfo != null)
            {
                AxisAlignedTransform2D componentTransform = componentInfo.transform.rotateClockwise(mazeTransform.getRotation());
                StructureBoundingBox compBoundingBox = getBoundingBox(coord, shift, roomSize, placedComponent, structureInfo, componentTransform, mazeTransform);
                NBTStorable instanceData = structureInfo.prepareInstanceData(new StructurePrepareContext(random, componentTransform, compBoundingBox, false));

                return new PlacedStructure(componentInfo.structureID, componentTransform, new BlockCoord(compBoundingBox.minX, compBoundingBox.minY, compBoundingBox.minZ), instanceData);
            }
            else
            {
                RecurrentComplex.logger.error(String.format("Could not find structure '%s' for maze", componentInfo.structureID));
            }

            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    protected static StructureBoundingBox getBoundingBox(BlockCoord coord, BlockCoord shift, int[] roomSize, ShiftedMazeComponent<MazeComponentStructure<Connector>, Connector> placedComponent, StructureInfo structureInfo, AxisAlignedTransform2D componentTransform, AxisAlignedTransform2D mazeTransform)
    {
        MazeRoom mazePosition = placedComponent.getShift();
        int[] scaledMazePosition = IvVecMathHelper.mul(mazePosition.getCoordinates(), roomSize);

        int[] structureBB = StructureInfos.structureSize(structureInfo, componentTransform);
        int[] compRoomSize = StructureInfos.structureSize(IvVecMathHelper.mul(placedComponent.getComponent().getSize(), roomSize), mazeTransform);
        int[] sizeDependentShift = new int[compRoomSize.length];
        for (int i = 0; i < structureBB.length; i++)
            sizeDependentShift[i] = (compRoomSize[i] - structureBB[i]) / 2;

        BlockCoord lowerCoord = StructureInfos.transformedLowerCoord(
                coord.add(mazeTransform.apply(shift.add(scaledMazePosition[0], scaledMazePosition[1], scaledMazePosition[2]), new int[]{1, 1, 1}))
                        .add(sizeDependentShift[0], sizeDependentShift[1], sizeDependentShift[2]),
                structureBB, mazeTransform);

        return StructureInfos.structureBoundingBox(lowerCoord, structureBB);
    }

    public static List<MazeComponentStructure<Connector>> transformedComponents(Collection<Pair<StructureInfo, MazeGenerationInfo>> componentStructures, ConnectorFactory factory, AxisAlignedTransform2D transform, Collection<Connector> blockedConnections)
    {
        List<MazeComponentStructure<Connector>> transformedComponents = new ArrayList<>();
        for (Pair<StructureInfo, MazeGenerationInfo> pair : componentStructures)
        {
            StructureInfo info = pair.getLeft();
            SavedMazeComponent comp = pair.getRight().mazeComponent;

            int[] compSize = comp.getSize();
            int roomVariations = (info.isRotatable() ? 4 : 1) * (info.isMirrorable() ? 2 : 1);

            double splitCompWeight = 0;
            if (comp.getWeight() > 0)
                splitCompWeight = comp.getWeight() / roomVariations;

            for (int rotations = 0; rotations < (info.isRotatable() ? 4 : 1); rotations++)
            {
                if (info.isRotatable() || (rotations + transform.getRotation()) % 4 == 0)
                {
                    transformedComponents.add(transformedComponent(info, comp, AxisAlignedTransform2D.from(rotations, false), compSize, splitCompWeight, factory, blockedConnections));

                    if (info.isMirrorable())
                        transformedComponents.add(transformedComponent(info, comp, AxisAlignedTransform2D.from(rotations, true), compSize, splitCompWeight, factory, blockedConnections));
                }
            }
        }

        return transformedComponents;
    }

    public static MazeComponentStructure<Connector> transformedComponent(StructureInfo info, SavedMazeComponent comp, final AxisAlignedTransform2D transform, final int[] size, double weight, ConnectorFactory factory, Collection<Connector> blockedConnections)
    {
        Set<MazeRoom> transformedRooms = comp.getRooms().stream().map(input -> MazeRooms.rotated(input, transform, size)).collect(Collectors.toSet());

        Map<MazePassage, Connector> transformedExits = new HashMap<>();
        comp.getExitPaths().stream().map(SavedMazePaths.buildFunction(factory)).forEach(path -> transformedExits.put(MazePassages.rotated(path.getKey(), transform, size), path.getValue()));
        addMissingExits(transformedRooms, transformedExits, comp.defaultConnector.toConnector(factory));

        ImmutableMultimap<MazePassage, MazePassage> reachability = comp.reachability.build(transform, size, SavedMazeReachability.notBlocked(blockedConnections, transformedExits), transformedExits.keySet());

        return new MazeComponentStructure<>(weight, StructureRegistry.INSTANCE.structureID(info), transform, ImmutableSet.copyOf(transformedRooms), ImmutableMap.copyOf(transformedExits), reachability);
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
}
