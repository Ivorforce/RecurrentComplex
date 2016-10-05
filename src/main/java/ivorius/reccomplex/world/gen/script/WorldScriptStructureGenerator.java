/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.script;

import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.blocks.Directions;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.ivtoolkit.tools.IvCollections;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.worldscripts.structuregenerator.TableDataSourceStructureGenerator;
import ivorius.reccomplex.world.gen.feature.structure.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.ListGenerationInfo;
import ivorius.reccomplex.utils.NBTStorable;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Created by lukas on 13.09.15.
 */
public class WorldScriptStructureGenerator implements WorldScript<WorldScriptStructureGenerator.InstanceData>
{
    protected boolean simpleMode;

    protected BlockPos structureShift = BlockPos.ORIGIN;

    // List Type
    protected String structureListID = "";
    protected EnumFacing front;

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

    public BlockPos getStructureShift()
    {
        return structureShift;
    }

    public void setStructureShift(BlockPos structureShift)
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

    public EnumFacing getFront()
    {
        return front;
    }

    public void setFront(EnumFacing front)
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
        structureShift = BlockPositions.readFromNBT("structureShift", nbtTagCompound);

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
        BlockPositions.writeToNBT("structureShift", structureShift, nbtTagCompound);

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
    public InstanceData prepareInstanceData(StructurePrepareContext context, BlockPos pos)
    {
        WorldScriptStructureGenerator.InstanceData instanceData = null;
        Random random = context.random;
        AxisAlignedTransform2D transform = context.transform;

        if (simpleMode)
        {
            if (structureNames.size() > 0)
            {
                String structureID = structureNames.get(random.nextInt(structureNames.size()));
                StructureInfo<?> structureInfo = StructureRegistry.INSTANCE.get(structureID);

                if (structureInfo != null)
                {
                    int rotations = structureInfo.isRotatable() ? (structureRotation != null ? transform.getRotation() + structureRotation : random.nextInt(4)) : 0;
                    boolean mirrorX = structureInfo.isMirrorable() && (structureMirror != null ? transform.isMirrorX() != structureMirror : random.nextBoolean());
                    AxisAlignedTransform2D strucTransform = AxisAlignedTransform2D.from(rotations, mirrorX);

                    int[] strucSize = structureInfo.structureBoundingBox();
                    BlockPos strucCoord = transform.apply(structureShift, new int[]{1, 1, 1})
                            .subtract(transform.apply(BlockPos.ORIGIN, strucSize)).add(pos);

                    instanceData = new WorldScriptStructureGenerator.InstanceData(structureID, null, strucCoord, strucTransform,
                            new StructureGenerator<>(structureInfo).random(random).environment(context.environment).transform(strucTransform).lowerCoord(strucCoord).asSource(context.generateAsSource)
                                    .instanceData().orElse(null));
                }
            }
        }
        else
        {
            Collection<Pair<StructureInfo, ListGenerationInfo>> generationInfos = StructureRegistry.INSTANCE.getStructuresInList(structureListID, front);

            if (generationInfos.size() > 0)
            {
                Pair<StructureInfo, ListGenerationInfo> pair = WeightedSelector.select(random, generationInfos, item ->
                        RCConfig.tweakedSpawnRate(StructureRegistry.INSTANCE.id(item.getLeft())) * item.getRight().getWeight());
                StructureInfo structureInfo = pair.getLeft();
                String structureID = StructureRegistry.INSTANCE.id(structureInfo);
                ListGenerationInfo generationInfo = pair.getRight();

                boolean mirrorX;
                int rotations;
                if (front != null)
                {
                    EnumFacing curFront = Directions.rotate(front, transform);
                    mirrorX = structureInfo.isMirrorable() && structureInfo.isRotatable() && random.nextBoolean();
                    Integer neededRotations = Directions.getHorizontalClockwiseRotations(curFront, generationInfo.front, mirrorX);
                    rotations = neededRotations != null ? neededRotations : 0;
                }
                else
                {
                    mirrorX = structureInfo.isMirrorable() && random.nextBoolean();
                    rotations = structureInfo.isRotatable() ? random.nextInt(4) : 0;
                }

                AxisAlignedTransform2D strucTransform = AxisAlignedTransform2D.from(rotations, mirrorX);

                int[] strucSize = structureInfo.structureBoundingBox();
                BlockPos strucCoord = transform.apply(structureShift.add(generationInfo.shift), new int[]{1, 1, 1})
                        .subtract(transform.apply(BlockPos.ORIGIN, strucSize)).add(pos);

                instanceData = new WorldScriptStructureGenerator.InstanceData(structureID, generationInfo.id(), strucCoord, strucTransform,
                        (NBTStorable) new StructureGenerator<>(structureInfo).random(random).environment(context.environment).transform(strucTransform).asSource(context.generateAsSource)
                                .lowerCoord(strucCoord).instanceData().orElse(null));
            }
        }

        return instanceData != null ? instanceData : new WorldScriptStructureGenerator.InstanceData();
    }

    @Override
    public void generate(StructureSpawnContext context, InstanceData instanceData, BlockPos coord)
    {
        StructureInfo structureInfo = StructureRegistry.INSTANCE.get(instanceData.structureID);
        NBTStorable structureData = instanceData.structureData;

        if (structureInfo != null && structureData != null)
            generate(context, instanceData, structureInfo, structureData, instanceData.generationInfoID);
    }

    @Nonnull
    protected <I extends NBTStorable> Optional<StructureSpawnContext> generate(StructureSpawnContext context, InstanceData instanceData, StructureInfo<I> structureInfo, I structureData, String generationInfo)
    {
        return new StructureGenerator<>(structureInfo).structureID(instanceData.structureID).asChild(context).generationInfo(generationInfo)
                .lowerCoord(instanceData.lowerCoord).transform(instanceData.structureTransform).instanceData(structureData).generate(
                );
    }

    @Override
    public String getDisplayString()
    {
        return IvTranslations.get("reccomplex.worldscript.strucGen");
    }

    @Override
    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate tableDelegate)
    {
        return new TableDataSourceStructureGenerator(this, navigator, tableDelegate);
    }

    public static class InstanceData implements NBTStorable
    {
        public String structureID;
        public String generationInfoID;
        public BlockPos lowerCoord;
        public AxisAlignedTransform2D structureTransform;

        public NBTStorable structureData;

        public InstanceData()
        {
            structureID = "";
        }

        public InstanceData(String structureID, String generationInfoID, BlockPos lowerCoord, AxisAlignedTransform2D structureTransform, NBTStorable structureData)
        {
            this.structureID = structureID;
            this.generationInfoID = generationInfoID;
            this.lowerCoord = lowerCoord;
            this.structureTransform = structureTransform;
            this.structureData = structureData;
        }

        public InstanceData(NBTTagCompound compound)
        {
            structureID = compound.getString("structureID");
            generationInfoID = compound.hasKey(generationInfoID, Constants.NBT.TAG_STRING) ? compound.getString("generationInfoID") : null;
            lowerCoord = BlockPositions.readFromNBT("lowerCoord", compound);
            structureTransform = AxisAlignedTransform2D.from(compound.getInteger("rotation"), compound.getBoolean("mirrorX"));

            StructureInfo structureInfo = StructureRegistry.INSTANCE.get(structureID);
            if (structureInfo != null)
                new StructureGenerator<>(structureInfo).instanceData(compound.getTag("structureData"))
                        .transform(structureTransform).lowerCoord(lowerCoord).instanceData().orElse(null);
        }

        @Override
        public NBTBase writeToNBT()
        {
            NBTTagCompound compound = new NBTTagCompound();

            compound.setString("structureID", structureID);
            if (generationInfoID != null) compound.setString("generationInfoID", generationInfoID);
            BlockPositions.writeToNBT("lowerCoord", lowerCoord, compound);
            compound.setInteger("rotation", structureTransform.getRotation());
            compound.setBoolean("mirrorX", structureTransform.isMirrorX());
            compound.setTag("structureData", structureData.writeToNBT());

            return compound;
        }
    }
}
