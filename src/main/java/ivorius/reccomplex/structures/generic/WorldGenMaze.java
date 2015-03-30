/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic;

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
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 27.06.14.
 */
public class WorldGenMaze
{
    public static boolean generateMazeInstantly(World world, Random random, BlockCoord coord, List<MazeComponentPosition> placedComponents, int[] roomSize, int layer)
    {
        return generateMaze(world, random, coord, placedComponents, roomSize, layer, null, false);
    }

    public static boolean generateMaze(World world, Random random, BlockCoord coord, List<MazeComponentPosition> placedComponents, int[] roomSize, int layer, StructureBoundingBox boundingBox, boolean firstTime)
    {
        int[] pathLengths = new int[]{0, 0, 0};

        for (MazeComponentPosition position : placedComponents)
        {
            MazeComponentInfo info = (MazeComponentInfo) position.getComponent().getIdentifier();

            StructureInfo compStructureInfo = StructureRegistry.getStructure(info.structureID);
            if (compStructureInfo != null)
            {
                MazeRoom mazePosition = position.getPositionInMaze();
//                int[] size = maze.getRoomSize(mazePosition, pathLengths, roomSize);
                int[] scaledCompMazePosition = Maze.getRoomPosition(mazePosition, pathLengths, roomSize);

                AxisAlignedTransform2D componentTransform = info.transform;

                String compStructureName = StructureRegistry.getName(compStructureInfo);

                int[] compStructureSize = StructureInfos.structureSize(compStructureInfo, componentTransform);
                int[] compRoomSize = Maze.getRoomSize(position.getComponent().getSize(), pathLengths, roomSize);
                int[] sizeDependentShift = new int[]{(compRoomSize[0] - compStructureSize[0]) / 2, (compRoomSize[1] - compStructureSize[1]) / 2, (compRoomSize[2] - compStructureSize[2]) / 2};

                BlockCoord compMazeCoordLower = coord.add(scaledCompMazePosition[0] + sizeDependentShift[0], scaledCompMazePosition[1] + sizeDependentShift[1], scaledCompMazePosition[2] + +sizeDependentShift[2]);

                if (boundingBox != null)
                    StructureGenerator.instantly(compStructureInfo, world, random, compMazeCoordLower, componentTransform, layer + 1, false, compStructureName);
                else
                {
                    StructureBoundingBox compBoundingBox = StructureInfos.structureBoundingBox(compMazeCoordLower, compStructureSize);
                    NBTStorable instanceData = compStructureInfo.loadInstanceData(new StructureLoadContext(componentTransform, compBoundingBox, false), info.instanceData);
                    StructureGenerator.partially(compStructureInfo, world, random, compMazeCoordLower, componentTransform, compBoundingBox, layer + 1, compStructureName, instanceData, firstTime);
                }
            }
            else
            {
                RecurrentComplex.logger.error(String.format("Could not find structure '%s' for maze", info.structureID));
            }
        }

        return true;
    }

    public static List<MazeComponent> transformedComponents(Collection<Pair<StructureInfo, MazeGenerationInfo>> componentStructures, StructurePrepareContext context)
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
                        transformedRooms.add(MazeGenerator.rotatedRoom(room, componentTransform, compSize));

                    List<MazePath> transformedExits = new ArrayList<>();
                    for (MazePath exit : comp.getExitPaths())
                        transformedExits.add(MazeGenerator.rotatedPath(exit, componentTransform, compSize));

                    MazeComponentInfo compInfo = new MazeComponentInfo(StructureRegistry.getName(info), componentTransform, info.prepareInstanceData(context).writeToNBT());
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
        public NBTBase instanceData;

        public MazeComponentInfo(String structureID, AxisAlignedTransform2D transform, NBTBase instanceData)
        {
            this.structureID = structureID;
            this.transform = transform;
            this.instanceData = instanceData;
        }

        @Override
        public void readFromNBT(NBTTagCompound compound)
        {
            structureID = compound.getString("structureID");
            transform = new AxisAlignedTransform2D(compound.getInteger("rotation"), compound.getBoolean("mirrorX"));
            instanceData = compound.getTag("instanceData");
        }

        @Override
        public void writeToNBT(NBTTagCompound compound)
        {
            compound.setString("structureID", structureID);
            compound.setInteger("rotation", transform.getRotation());
            compound.setBoolean("mirrorX", transform.isMirrorX());
            compound.setTag("instanceData", instanceData);
        }
    }
}
