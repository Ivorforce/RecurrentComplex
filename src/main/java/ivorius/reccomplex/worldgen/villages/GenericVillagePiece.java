/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.villages;

import ivorius.ivtoolkit.blocks.Directions;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.VanillaStructureGenerationInfo;
import ivorius.reccomplex.utils.NBTStorable;
import ivorius.reccomplex.worldgen.StructureGenerationData;
import ivorius.reccomplex.worldgen.StructureGenerator;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
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

    public void setIds(String structureID, String generationID)
    {
        this.structureID = structureID;
        this.generationID = generationID;
    }

    public void setOrientation(EnumFacing front, boolean mirrorX, StructureBoundingBox boundingBox)
    {
        setCoordBaseMode(front);
        this.mirrorX = mirrorX;
        this.boundingBox = boundingBox;
    }

    public void prepare(Random random, WorldServer world)
    {
        StructureInfo structureInfo = StructureRegistry.INSTANCE.getStructure(structureID);
        if (structureInfo != null)
        {
            StructureGenerationInfo generationInfo = structureInfo.generationInfo(generationID);

            if (generationInfo instanceof VanillaStructureGenerationInfo)
            {
                VanillaStructureGenerationInfo vanillaGenInfo = (VanillaStructureGenerationInfo) generationInfo;
                AxisAlignedTransform2D transform = getTransform(vanillaGenInfo, getCoordBaseMode().getOpposite(), mirrorX);

                instanceData = structureInfo.prepareInstanceData(new StructurePrepareContext(random, world, startPiece.biome, transform, boundingBox, false)).writeToNBT();
            }
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean addComponentParts(World world, Random random, StructureBoundingBox boundingBox)
    {
        StructureInfo structureInfo = StructureRegistry.INSTANCE.getStructure(structureID);
        if (structureInfo != null)
        {
            StructureGenerationInfo generationInfo = structureInfo.generationInfo(generationID);

            if (generationInfo instanceof VanillaStructureGenerationInfo)
            {
                VanillaStructureGenerationInfo vanillaGenInfo = (VanillaStructureGenerationInfo) generationInfo;
                AxisAlignedTransform2D transform = getTransform(vanillaGenInfo, getCoordBaseMode().getOpposite(), mirrorX);

                BlockPos structureShift = transform.apply(vanillaGenInfo.spawnShift, new int[]{1, 1, 1});

                if (this.averageGroundLvl < 0)
                {
                    this.averageGroundLvl = this.getAverageGroundLevel(world, boundingBox);

                    if (this.averageGroundLvl < 0)
                        return true;

                    // Structure shift y was included in bounding box, but must be re-added because it is overwritten
                    this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.minY + structureShift.getY(), 0);
                }

                if (world instanceof WorldServer)
                    generate((WorldServer) world, random, boundingBox, structureInfo, transform);

                return true;
            }
        }

        return false;
    }

    protected <T extends NBTStorable> void generate(WorldServer world, Random random, StructureBoundingBox boundingBox, StructureInfo<T> structureInfo, AxisAlignedTransform2D transform)
    {
        if (structureID != null && !startedGeneration)
            prepare(random, world);

        BlockPos lowerCoord = new BlockPos(this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ);
        T instanceData = structureInfo.loadInstanceData(new StructureLoadContext(transform, boundingBox, false), this.instanceData);

        StructureGenerator.partially(structureInfo, (WorldServer) world, random, lowerCoord, transform, boundingBox, componentType, structureID, instanceData, !startedGeneration);

        if (structureID != null && !startedGeneration)
            StructureGenerationData.get(world).addCompleteEntry(structureID, lowerCoord, transform);

        startedGeneration = true;
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
