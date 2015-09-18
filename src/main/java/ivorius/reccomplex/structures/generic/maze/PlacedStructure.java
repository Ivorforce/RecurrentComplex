/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.maze;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.StructureInfos;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

/**
 * Created by lukas on 16.04.15.
 */
public class PlacedStructure implements NBTCompoundObject
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

        StructureInfo structureInfo = StructureRegistry.INSTANCE.getStructure(structureID);

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
