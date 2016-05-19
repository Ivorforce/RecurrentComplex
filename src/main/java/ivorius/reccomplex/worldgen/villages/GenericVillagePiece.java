/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.villages;

import net.minecraft.util.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.structures.*;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.VanillaStructureGenerationInfo;
import ivorius.ivtoolkit.blocks.Directions;
import ivorius.reccomplex.utils.NBTStorable;
import ivorius.reccomplex.worldgen.StructureGenerationData;
import ivorius.reccomplex.worldgen.StructureGenerator;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Created by lukas on 18.01.15.
 */
public class GenericVillagePiece extends StructureVillagePieces.Village
{
    public String structureID;
    public String generationID;

    public boolean mirrorX;
    public boolean startedGeneration;
    public NBTBase instanceData;

    public GenericVillagePiece()
    {
    }

    public GenericVillagePiece(StructureVillagePieces.Start start, int generationDepth)
    {
        super(start, generationDepth);
    }

    public void setIds(String structureID, String generationID)
    {
        this.structureID = structureID;
        this.generationID = generationID;
    }

    public void setOrientation(EnumFacing front, boolean mirrorX, StructureBoundingBox boundingBox)
    {
        coordBaseMode = front;
        this.mirrorX = mirrorX;
        this.boundingBox = boundingBox;
    }

    public void prepare(Random random)
    {
        StructureInfo structureInfo = StructureRegistry.INSTANCE.getStructure(structureID);
        if (structureInfo != null)
        {
            StructureGenerationInfo generationInfo = structureInfo.generationInfo(generationID);

            if (generationInfo instanceof VanillaStructureGenerationInfo)
            {
                VanillaStructureGenerationInfo vanillaGenInfo = (VanillaStructureGenerationInfo) generationInfo;
                AxisAlignedTransform2D transform = getTransform(vanillaGenInfo, coordBaseMode, mirrorX);

                instanceData = structureInfo.prepareInstanceData(new StructurePrepareContext(random, transform, boundingBox, false)).writeToNBT();
            }
        }
    }

    public static AxisAlignedTransform2D getTransform(VanillaStructureGenerationInfo vanillaGenInfo, EnumFacing front, boolean mirrorX)
    {
        return AxisAlignedTransform2D.from(getRotations(vanillaGenInfo, front, mirrorX), mirrorX);
    }

    public static int getRotations(VanillaStructureGenerationInfo vanillaGenInfo, EnumFacing front, boolean mirrorX)
    {
        Integer rotations = Directions.getHorizontalClockwiseRotations(vanillaGenInfo.front, front, mirrorX);
        return rotations == null ? 0 : rotations;
    }

    @Nullable
    public static GenericVillagePiece create(String structureID, String generationID)
    {
        return VanillaGenerationClassFactory.instance().create(structureID, generationID);
    }

    @Nullable
    public static GenericVillagePiece create(String structureID, String generationID, StructureVillagePieces.Start start, int generationDepth)
    {
        return VanillaGenerationClassFactory.instance().create(structureID, generationID, start, generationDepth);
    }

    public static boolean canVillageGoDeeperC(StructureBoundingBox box)
    {
        return canVillageGoDeeper(box);
    }

    @Override
    public boolean addComponentParts(World world, Random random, StructureBoundingBox boundingBox)
    {
        StructureInfo structureInfo = StructureRegistry.INSTANCE.getStructure(structureID);
        if (structureInfo != null)
        {
            StructureGenerationInfo generationInfo = structureInfo.generationInfo(generationID);

            if (generationInfo instanceof VanillaStructureGenerationInfo)
            {
                VanillaStructureGenerationInfo vanillaGenInfo = (VanillaStructureGenerationInfo) generationInfo;
                AxisAlignedTransform2D transform = getTransform(vanillaGenInfo, coordBaseMode, mirrorX);

                BlockPos structureShift = transform.apply(vanillaGenInfo.spawnShift, new int[]{1, 1, 1});

                if (this.field_143015_k < 0)
                {
                    this.field_143015_k = this.getAverageGroundLevel(world, boundingBox);

                    if (this.field_143015_k < 0)
                        return true;

                    this.boundingBox.offset(0, this.field_143015_k - this.boundingBox.minY + structureShift.getY(), 0);
                }

                BlockPos lowerCoord = new BlockPos(this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ);
                NBTStorable instanceData = structureInfo.loadInstanceData(new StructureLoadContext(transform, boundingBox, false), this.instanceData);

                StructureGenerator.partially(structureInfo, world, random, lowerCoord, transform, boundingBox, componentType, structureID, instanceData, !startedGeneration);

                if (structureID != null && !startedGeneration)
                    StructureGenerationData.get(world).addCompleteEntry(structureID, lowerCoord, transform);

                startedGeneration = true;

                return true;
            }
        }

        return false;
    }

    @Override
    protected void writeStructureToNBT(NBTTagCompound tagCompound)
    {
        super.writeStructureToNBT(tagCompound);
        tagCompound.setString("RcSId", structureID);
        tagCompound.setString("RcGtId", structureID);
        tagCompound.setBoolean("RcMirror", mirrorX);
        tagCompound.setBoolean("RcStartGen", startedGeneration);
        tagCompound.setTag("RcInstDat", instanceData);
    }

    @Override
    protected void readStructureFromNBT(NBTTagCompound tagCompound)
    {
        super.readStructureFromNBT(tagCompound);
        structureID = tagCompound.getString("RcSId");
        generationID = tagCompound.getString("RcGtId");
        mirrorX = tagCompound.getBoolean("RcMirror");
        startedGeneration = tagCompound.getBoolean("RcStartGen");
        instanceData = tagCompound.getTag("RcInstDat");
    }
}
