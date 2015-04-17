/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.maze.components.*;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.structures.*;
import ivorius.reccomplex.structures.generic.gentypes.MazeGenerationInfo;
import ivorius.reccomplex.utils.NBTStorable;
import ivorius.reccomplex.worldgen.StructureGenerator;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by lukas on 27.06.14.
 */
public class WorldGenMaze
{
    public static boolean generatePlacedStructures(List<PlacedStructure> placedStructures, StructureSpawnContext context)
    {
        for (PlacedStructure placedComponent : placedStructures)
        {
            StructureInfo structureInfo = StructureRegistry.getStructure(placedComponent.structureID);

            if (structureInfo != null && placedComponent.instanceData != null)
            {
                AxisAlignedTransform2D componentTransform = placedComponent.transform;
                StructureBoundingBox compBoundingBox = StructureInfos.structureBoundingBox(placedComponent.lowerCoord, StructureInfos.structureSize(structureInfo, componentTransform));

                StructureGenerator.partially(structureInfo, context.world, context.random, new BlockCoord(compBoundingBox.minX, compBoundingBox.minY, compBoundingBox.minZ), componentTransform, context.generationBB, context.generationLayer + 1, placedComponent.structureID, placedComponent.instanceData, context.isFirstTime);
            }
            else
            {
                RecurrentComplex.logger.error(String.format("Could not find structure '%s' for maze", placedComponent.structureID));
            }
        }

        return true;
    }

    public static List<PlacedStructure> convertToPlacedStructures(final Random random, final BlockCoord coord, List<ShiftedMazeComponent<MazeComponentStructure<Connector>, Connector>> placedComponents, final int[] roomSize)
    {
        return Lists.newArrayList(Iterables.filter(Iterables.transform(placedComponents, new Function<ShiftedMazeComponent<MazeComponentStructure<Connector>, Connector>, PlacedStructure>()
        {
            @Nullable
            @Override
            public PlacedStructure apply(ShiftedMazeComponent<MazeComponentStructure<Connector>, Connector> placedComponent)
            {
                MazeComponentStructure<Connector> componentInfo = placedComponent.getComponent();
                StructureInfo structureInfo = StructureRegistry.getStructure(componentInfo.structureID);

                if (structureInfo != null)
                {
                    AxisAlignedTransform2D componentTransform = componentInfo.transform;
                    StructureBoundingBox compBoundingBox = getBoundingBox(coord, roomSize, placedComponent, structureInfo, componentTransform);
                    NBTStorable instanceData = structureInfo.prepareInstanceData(new StructurePrepareContext(random, componentTransform, compBoundingBox, false));

                    return new PlacedStructure(componentInfo.structureID, componentTransform, new BlockCoord(compBoundingBox.minX, compBoundingBox.minY, compBoundingBox.minZ), instanceData);
                }
                else
                {
                    RecurrentComplex.logger.error(String.format("Could not find structure '%s' for maze", componentInfo.structureID));
                }

                return null;
            }
        }), Predicates.notNull()));
    }

    protected static StructureBoundingBox getBoundingBox(BlockCoord coord, int[] roomSize, ShiftedMazeComponent<MazeComponentStructure<Connector>, Connector> placedComponent, StructureInfo structureInfo, AxisAlignedTransform2D componentTransform)
    {
        MazeRoom mazePosition = placedComponent.getShift();
        int[] scaledCompMazePosition = IvVecMathHelper.mul(mazePosition.getCoordinates(), roomSize);

        int[] compStructureSize = StructureInfos.structureSize(structureInfo, componentTransform);
        int[] compRoomSize = IvVecMathHelper.mul(placedComponent.getComponent().getSize(), roomSize);
        int[] sizeDependentShift = new int[]{(compRoomSize[0] - compStructureSize[0]) / 2, (compRoomSize[1] - compStructureSize[1]) / 2, (compRoomSize[2] - compStructureSize[2]) / 2};

        BlockCoord compMazeCoordLower = coord.add(scaledCompMazePosition[0] + sizeDependentShift[0], scaledCompMazePosition[1] + sizeDependentShift[1], scaledCompMazePosition[2] + +sizeDependentShift[2]);

        return StructureInfos.structureBoundingBox(compMazeCoordLower, compStructureSize);
    }

    public static List<MazeComponentStructure<Connector>> transformedComponents(Collection<Pair<StructureInfo, MazeGenerationInfo>> componentStructures, Connector roomConnector, Connector wallConnector)
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
                transformedComponents.add(structureComponent(info, comp, AxisAlignedTransform2D.transform(rotations, false), compSize, splitCompWeight, roomConnector, wallConnector));
                if( info.isMirrorable())
                    transformedComponents.add(structureComponent(info, comp, AxisAlignedTransform2D.transform(rotations, true), compSize, splitCompWeight, roomConnector, wallConnector));
            }
        }

        return transformedComponents;
    }

    public static MazeComponentStructure<Connector> structureComponent(StructureInfo info, SavedMazeComponent comp, AxisAlignedTransform2D transform, int[] size, double weight, Connector roomConnector, Connector wallConnector)
    {
        Set<MazeRoom> transformedRooms = new HashSet<>();
        for (MazeRoom room : comp.getRooms())
            transformedRooms.add(MazeRooms.rotated(room, transform, size));

        Map<MazeRoomConnection, Connector> transformedExits = new HashMap<>();
        for (Map.Entry<MazeRoomConnection, Connector> exit : Lists.transform(comp.getExitPaths(), SavedMazePaths.toConnectionFunction(roomConnector)))
            transformedExits.put(MazeRoomConnections.rotated(exit.getKey(), transform, size), roomConnector);

        MazeComponentStructure<Connector> component = new MazeComponentStructure<>(weight, StructureRegistry.structureID(info), transform, transformedRooms, transformedExits);
        addMissingExits(component, wallConnector);
        return component;
    }

    public static <C> SetMazeComponent<C> createCompleteComponent(Set<MazeRoom> rooms, Map<MazeRoomConnection, C> exits, C wallConnector)
    {
        SetMazeComponent<C> component = new SetMazeComponent<>(rooms, exits);
        addMissingExits(component, wallConnector);
        return component;
    }

    public static <C> void addMissingExits(MazeComponent<C> component, C wallConnector)
    {
        for (MazeRoom room : component.rooms())
            for (MazeRoomConnection connection : MazeRooms.neighbors(room))
                if (!component.exits().containsKey(connection))
                    component.exits().put(connection, wallConnector);
    }
}
