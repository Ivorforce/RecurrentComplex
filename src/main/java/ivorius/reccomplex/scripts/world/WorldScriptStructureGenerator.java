/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.scripts.world;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.blocks.Directions;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.ivtoolkit.tools.IvCollections;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.worldscripts.structuregenerator.TableDataSourceStructureBlock;
import ivorius.reccomplex.structures.*;
import ivorius.reccomplex.structures.generic.gentypes.StructureListGenerationInfo;
import ivorius.reccomplex.utils.IvTranslations;
import ivorius.reccomplex.utils.NBTStorable;
import ivorius.reccomplex.worldgen.StructureGenerator;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Created by lukas on 13.09.15.
 */
public class WorldScriptStructureGenerator implements WorldScript<WorldScriptStructureGenerator.InstanceData>
{
    protected boolean simpleMode;

    protected BlockCoord structureShift = new BlockCoord(0, 0, 0);

    // List Type
    protected String structureListID = "";
    protected ForgeDirection front;

    // Simple Type
    protected List<String> structureNames = new ArrayList<>();
    protected Integer structureRotation;
    protected Boolean structureMirror;

    public boolean isSimpleMode()
    {
        return simpleMode;
    }

    public void setSimpleMode(boolean simpleMode)
    {
        this.simpleMode = simpleMode;
    }

    public BlockCoord getStructureShift()
    {
        return structureShift;
    }

    public void setStructureShift(BlockCoord structureShift)
    {
        this.structureShift = structureShift;
    }

    public String getStructureListID()
    {
        return structureListID;
    }

    public void setStructureListID(String structureListID)
    {
        this.structureListID = structureListID;
    }

    public ForgeDirection getFront()
    {
        return front;
    }

    public void setFront(ForgeDirection front)
    {
        this.front = front;
    }

    public List<String> getStructureNames()
    {
        return Collections.unmodifiableList(structureNames);
    }

    public void setStructureNames(List<String> structureNames)
    {
        IvCollections.setContentsOfList(this.structureNames, structureNames);
    }

    public Integer getStructureRotation()
    {
        return structureRotation;
    }

    public void setStructureRotation(Integer structureRotation)
    {
        this.structureRotation = structureRotation;
    }

    public Boolean getStructureMirror()
    {
        return structureMirror;
    }

    public void setStructureMirror(Boolean structureMirror)
    {
        this.structureMirror = structureMirror;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        structureShift = BlockCoord.readCoordFromNBT("structureShift", nbtTagCompound);

        simpleMode = !nbtTagCompound.hasKey("simpleMode", Constants.NBT.TAG_BYTE)
                || nbtTagCompound.getBoolean("simpleMode"); // Legacy

        structureListID = nbtTagCompound.getString("structureListID");
        front = nbtTagCompound.hasKey("front", Constants.NBT.TAG_STRING) ? Directions.deserialize(nbtTagCompound.getString("front")) : null;

        List<String> structureList = new ArrayList<>();
        NBTTagList structureNBTList = nbtTagCompound.getTagList("structures", Constants.NBT.TAG_STRING);
        for (int i = 0; i < structureNBTList.tagCount(); i++)
            structureList.add(structureNBTList.getStringTagAt(i));
        IvCollections.setContentsOfList(structureNames, structureList);

        structureRotation = nbtTagCompound.hasKey("structureRotation") ? nbtTagCompound.getInteger("structureRotation") : null;
        structureMirror = nbtTagCompound.hasKey("structureMirror") ? nbtTagCompound.getBoolean("structureMirror") : null;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        BlockCoord.writeCoordToNBT("structureShift", structureShift, nbtTagCompound);

        nbtTagCompound.setBoolean("simpleMode", simpleMode);

        nbtTagCompound.setString("structureListID", structureListID);
        if (front != null)
            nbtTagCompound.setString("front", Directions.serialize(front));

        NBTTagList structureNBTList = new NBTTagList();
        for (String struc : structureNames)
            structureNBTList.appendTag(new NBTTagString(struc));
        nbtTagCompound.setTag("structures", structureNBTList);

        if (structureRotation != null)
            nbtTagCompound.setInteger("structureRotation", structureRotation);

        if (structureMirror != null)
            nbtTagCompound.setBoolean("structureMirror", structureMirror);
    }

    @Override
    public WorldScriptStructureGenerator.InstanceData loadInstanceData(StructureLoadContext context, NBTBase nbt)
    {
        return new WorldScriptStructureGenerator.InstanceData(nbt instanceof NBTTagCompound ? (NBTTagCompound) nbt : new NBTTagCompound());
    }

    @Override
    public InstanceData prepareInstanceData(StructurePrepareContext context, BlockCoord coord, World world)
    {
        WorldScriptStructureGenerator.InstanceData instanceData = null;
        Random random = context.random;
        AxisAlignedTransform2D transform = context.transform;

        if (simpleMode)
        {
            if (structureNames.size() > 0)
            {
                String structureID = structureNames.get(random.nextInt(structureNames.size()));
                StructureInfo structureInfo = StructureRegistry.getStructure(structureID);

                if (structureInfo != null)
                {
                    int rotations = structureInfo.isRotatable() ? (structureRotation != null ? transform.getRotation() + structureRotation : random.nextInt(4)) : 0;
                    boolean mirrorX = structureInfo.isMirrorable() && (structureMirror != null ? transform.isMirrorX() != structureMirror : random.nextBoolean());
                    AxisAlignedTransform2D strucTransform = AxisAlignedTransform2D.transform(rotations, mirrorX);

                    int[] strucSize = structureInfo.structureBoundingBox();
                    BlockCoord strucCoord = transform.apply(structureShift, new int[]{1, 1, 1})
                            .subtract(transform.apply(new BlockCoord(0, 0, 0), strucSize));

                    instanceData = new WorldScriptStructureGenerator.InstanceData(structureID, strucCoord, strucTransform, structureInfo.prepareInstanceData(context));
                }
            }
        }
        else
        {
            Collection<Pair<StructureInfo, StructureListGenerationInfo>> generationInfos = StructureRegistry.getStructuresInList(structureListID, front);

            if (generationInfos.size() > 0)
            {
                Pair<StructureInfo, StructureListGenerationInfo> pair = WeightedSelector.select(random, generationInfos, new WeightedSelector.WeightFunction<Pair<StructureInfo, StructureListGenerationInfo>>()
                {
                    @Override
                    public double apply(Pair<StructureInfo, StructureListGenerationInfo> item)
                    {
                        return item.getRight().getWeight();
                    }
                });
                StructureInfo structureInfo = pair.getLeft();
                String structureID = StructureRegistry.structureID(structureInfo);
                StructureListGenerationInfo generationInfo = pair.getRight();

                boolean mirrorX;
                int rotations;
                if (front != null)
                {
                    ForgeDirection curFront = Directions.rotate(front, transform);
                    mirrorX = structureInfo.isMirrorable() && structureInfo.isRotatable() && random.nextBoolean();
                    Integer neededRotations = Directions.getHorizontalClockwiseRotations(curFront, generationInfo.front, mirrorX);
                    rotations = neededRotations != null ? neededRotations : 0;
                }
                else
                {
                    mirrorX = structureInfo.isMirrorable() && random.nextBoolean();
                    rotations = structureInfo.isRotatable() ? random.nextInt(4) : 0;
                }

                AxisAlignedTransform2D strucTransform = AxisAlignedTransform2D.transform(rotations, mirrorX);

                int[] strucSize = structureInfo.structureBoundingBox();
                BlockCoord strucCoord = transform.apply(structureShift.add(generationInfo.shiftX, generationInfo.shiftY, generationInfo.shiftZ), new int[]{1, 1, 1})
                        .subtract(transform.apply(new BlockCoord(0, 0, 0), strucSize));

                instanceData = new WorldScriptStructureGenerator.InstanceData(structureID, strucCoord, strucTransform, structureInfo.prepareInstanceData(context));
            }
        }

        return instanceData != null ? instanceData : new WorldScriptStructureGenerator.InstanceData();
    }

    @Override
    public void generate(StructureSpawnContext context, InstanceData instanceData, BlockCoord coord)
    {
        World world = context.world;
        Random random = context.random;
        int layer = context.generationLayer;

        StructureInfo structureInfo = StructureRegistry.getStructure(instanceData.structureID);
        if (structureInfo != null && instanceData.structureData != null)
            StructureGenerator.partially(structureInfo, world, random, instanceData.lowerCoord.add(coord), instanceData.structureTransform, context.generationBB, layer + 1, instanceData.structureID, instanceData.structureData, context.isFirstTime);
    }

    @Override
    public String getDisplayString()
    {
        return IvTranslations.get("reccomplex.worldscript.strucGen");
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate tableDelegate)
    {
        return new TableDataSourceStructureBlock(this, navigator, tableDelegate);
    }

    public static class InstanceData implements NBTStorable
    {
        public String structureID;
        public BlockCoord lowerCoord;
        public AxisAlignedTransform2D structureTransform;

        public NBTStorable structureData;

        public InstanceData()
        {
            structureID = "";
        }

        public InstanceData(String structureID, BlockCoord lowerCoord, AxisAlignedTransform2D structureTransform, NBTStorable structureData)
        {
            this.structureID = structureID;
            this.lowerCoord = lowerCoord;
            this.structureTransform = structureTransform;
            this.structureData = structureData;
        }

        public InstanceData(NBTTagCompound compound)
        {
            structureID = compound.getString("structureID");
            lowerCoord = BlockCoord.readCoordFromNBT("lowerCoord", compound);
            structureTransform = new AxisAlignedTransform2D(compound.getInteger("rotation"), compound.getBoolean("mirrorX"));

            StructureInfo structureInfo = StructureRegistry.getStructure(structureID);
            if (structureInfo != null)
                structureData = structureInfo.loadInstanceData(new StructureLoadContext(structureTransform, StructureInfos.structureBoundingBox(lowerCoord, StructureInfos.structureSize(structureInfo, structureTransform)), false), compound.getTag("structureData"));
        }

        @Override
        public NBTBase writeToNBT()
        {
            NBTTagCompound compound = new NBTTagCompound();

            compound.setString("structureID", structureID);
            BlockCoord.writeCoordToNBT("lowerCoord", lowerCoord, compound);
            compound.setInteger("rotation", structureTransform.getRotation());
            compound.setBoolean("mirrorX", structureTransform.isMirrorX());
            compound.setTag("structureData", structureData.writeToNBT());

            return compound;
        }
    }
}
