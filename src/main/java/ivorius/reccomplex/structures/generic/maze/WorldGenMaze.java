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
import ivorius.ivtoolkit.maze.*;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.structures.*;
import ivorius.reccomplex.structures.generic.gentypes.MazeGenerationInfo;
import ivorius.reccomplex.utils.NBTStorable;
import ivorius.reccomplex.worldgen.StructureGenerator;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

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

    public static List<PlacedStructure> convertToPlacedStructures(final Random random, final BlockCoord coord, List<MazeComponentPosition> placedComponents, final int[] roomSize)
    {
        final int[] pathLengths = new int[]{0, 0, 0};

        return Lists.newArrayList(Iterables.filter(Iterables.transform(placedComponents, new Function<MazeComponentPosition, PlacedStructure>()
        {
            @Nullable
            @Override
            public PlacedStructure apply(MazeComponentPosition placedComponent)
            {
                MazeComponentInfo componentInfo = (WorldGenMaze.MazeComponentInfo) placedComponent.getComponent().getIdentifier();
                StructureInfo structureInfo = StructureRegistry.getStructure(componentInfo.structureID);

                if (structureInfo != null)
                {
                    AxisAlignedTransform2D componentTransform = componentInfo.transform;
                    StructureBoundingBox compBoundingBox = getBoundingBox(coord, roomSize, pathLengths, placedComponent, structureInfo, componentTransform);
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

    protected static StructureBoundingBox getBoundingBox(BlockCoord coord, int[] roomSize, int[] pathLengths, MazeComponentPosition placedComponent, StructureInfo structureInfo, AxisAlignedTransform2D componentTransform)
    {
        MazeRoom mazePosition = placedComponent.getPositionInMaze();
        int[] scaledCompMazePosition = Maze.getRoomPosition(mazePosition, pathLengths, roomSize);

        int[] compStructureSize = StructureInfos.structureSize(structureInfo, componentTransform);
        int[] compRoomSize = Maze.getRoomSize(placedComponent.getComponent().getSize(), pathLengths, roomSize);
        int[] sizeDependentShift = new int[]{(compRoomSize[0] - compStructureSize[0]) / 2, (compRoomSize[1] - compStructureSize[1]) / 2, (compRoomSize[2] - compStructureSize[2]) / 2};

        BlockCoord compMazeCoordLower = coord.add(scaledCompMazePosition[0] + sizeDependentShift[0], scaledCompMazePosition[1] + sizeDependentShift[1], scaledCompMazePosition[2] + +sizeDependentShift[2]);

        return StructureInfos.structureBoundingBox(compMazeCoordLower, compStructureSize);
    }

    public static List<MazeComponent> transformedComponents(Collection<Pair<StructureInfo, MazeGenerationInfo>> componentStructures)
    {
        List<MazeComponent> transformedComponents = new ArrayList<>();
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
                for (int mirrorInd = 0; mirrorInd < (info.isMirrorable() ? 2 : 1); mirrorInd++)
                {
                    AxisAlignedTransform2D componentTransform = AxisAlignedTransform2D.transform(rotations, mirrorInd == 1);

                    List<MazeRoom> transformedRooms = new ArrayList<>();
                    for (MazeRoom room : comp.getRooms())
                        transformedRooms.add(MazeCoordinates.rotatedRoom(room, componentTransform, compSize));

                    List<MazePath> transformedExits = new ArrayList<>();
                    for (MazePath exit : Lists.transform(comp.getExitPaths(), SavedMazePaths.toPathFunction()))
                        transformedExits.add(MazeCoordinates.rotatedPath(exit, componentTransform, compSize));

                    MazeComponentInfo compInfo = new MazeComponentInfo(StructureRegistry.structureID(info), componentTransform, null);
                    transformedComponents.add(new MazeComponent(splitCompWeight, compInfo, transformedRooms, transformedExits));
                }
            }
        }

        return transformedComponents;
    }

    public static class MazeComponentInfo implements NBTCompoundObject
    {
        public String structureID;
        public AxisAlignedTransform2D transform;

        public MazeComponentInfo()
        {
        }

        public MazeComponentInfo(String structureID, AxisAlignedTransform2D transform, NBTBase instanceData)
        {
            this.structureID = structureID;
            this.transform = transform;
        }

        @Override
        public void readFromNBT(NBTTagCompound compound)
        {
            structureID = compound.getString("structureID");
            transform = new AxisAlignedTransform2D(compound.getInteger("rotation"), compound.getBoolean("mirrorX"));
        }

        @Override
        public void writeToNBT(NBTTagCompound compound)
        {
            compound.setString("structureID", structureID);
            compound.setInteger("rotation", transform.getRotation());
            compound.setBoolean("mirrorX", transform.isMirrorX());
        }
    }

    public static class PlacedStructure implements NBTCompoundObject
    {
        public String structureID;
        public AxisAlignedTransform2D transform;
        public BlockCoord lowerCoord;

        public NBTStorable instanceData;

        public PlacedStructure(String structureID, AxisAlignedTransform2D transform, BlockCoord lowerCoord, NBTStorable instanceData)
        {
            this.structureID = structureID;
            this.transform = transform;
            this.lowerCoord = lowerCoord;
            this.instanceData = instanceData;
        }

        @Override
        public void readFromNBT(NBTTagCompound compound)
        {
            structureID = compound.getString("structureID");
            transform = new AxisAlignedTransform2D(compound.getInteger("rotation"), compound.getBoolean("mirrorX"));
            lowerCoord = BlockCoord.readCoordFromNBT("lowerCoord", compound);

            StructureInfo structureInfo = StructureRegistry.getStructure(structureID);

            instanceData = compound.hasKey("instanceData", Constants.NBT.TAG_COMPOUND) && structureInfo != null
                    ? structureInfo.loadInstanceData(new StructureLoadContext(transform, StructureInfos.structureBoundingBox(lowerCoord, StructureInfos.structureSize(structureInfo, transform)), false), compound.getTag("instanceData"))
                    : null;
        }

        @Override
        public void writeToNBT(NBTTagCompound compound)
        {
            compound.setString("structureID", structureID);
            compound.setInteger("rotation", transform.getRotation());
            compound.setBoolean("mirrorX", transform.isMirrorX());
            BlockCoord.writeCoordToNBT("lowerCoord", lowerCoord, compound);
            if (instanceData != null)
                compound.setTag("instanceData", instanceData.writeToNBT());
        }
    }
}
