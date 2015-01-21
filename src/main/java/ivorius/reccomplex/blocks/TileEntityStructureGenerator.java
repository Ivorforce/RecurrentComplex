/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.IvCollections;
import ivorius.reccomplex.worldgen.StructureRegistry;
import ivorius.reccomplex.worldgen.StructureInfo;
import ivorius.reccomplex.worldgen.WorldGenStructures;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 06.06.14.
 */
public class TileEntityStructureGenerator extends TileEntity implements GeneratingTileEntity
{
    private List<String> structureNames = new ArrayList<>();
    private BlockCoord structureShift = new BlockCoord(0, 0, 0);
    private Integer structureRotation;
    private Boolean structureMirror;

    public List<String> getStructureNames()
    {
        return Collections.unmodifiableList(structureNames);
    }

    public void setStructureNames(List<String> structureNames)
    {
        IvCollections.setContentsOfList(this.structureNames, structureNames);
    }

    public BlockCoord getStructureShift()
    {
        return structureShift;
    }

    public void setStructureShift(BlockCoord structureShift)
    {
        this.structureShift = structureShift;
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
        super.readFromNBT(nbtTagCompound);

        readStructureDataFromNBT(nbtTagCompound);
    }

    public void readStructureDataFromNBT(NBTTagCompound nbtTagCompound)
    {
        IvCollections.setContentsOfList(structureNames, generatorsFromNBT(nbtTagCompound));

        structureShift = BlockCoord.readCoordFromNBT("structureShift", nbtTagCompound);

        structureRotation = nbtTagCompound.hasKey("structureRotation") ? nbtTagCompound.getInteger("structureRotation") : null;
        structureMirror = nbtTagCompound.hasKey("structureMirror") ? nbtTagCompound.getBoolean("structureMirror") : null;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        super.writeToNBT(nbtTagCompound);

        writeStructureDataToNBT(nbtTagCompound);
    }

    public void writeStructureDataToNBT(NBTTagCompound nbtTagCompound)
    {
        NBTTagList structureNBTList = new NBTTagList();
        for (String struc : structureNames)
        {
            structureNBTList.appendTag(new NBTTagString(struc));
        }
        nbtTagCompound.setTag("structures", structureNBTList);

        BlockCoord.writeCoordToNBT("structureShift", structureShift, nbtTagCompound);

        if (structureRotation != null)
        {
            nbtTagCompound.setInteger("structureRotation", structureRotation);
        }

        if (structureMirror != null)
        {
            nbtTagCompound.setBoolean("structureMirror", structureMirror);
        }
    }

    @Override
    public void generate(World world, Random random, AxisAlignedTransform2D transform, int layer)
    {
        world.setBlockToAir(xCoord, yCoord, zCoord);

        if (structureNames.size() > 0)
        {
            String structure = structureNames.get(random.nextInt(structureNames.size()));
            StructureInfo structureInfo = StructureRegistry.getStructure(structure);

            if (structureInfo != null)
            {
                int strucRotation = structureInfo.isRotatable() ? (structureRotation != null ? transform.getRotation() + structureRotation : random.nextInt(4)) : 0;
                boolean strucMirror = structureInfo.isMirrorable() && (structureMirror != null ? transform.isMirrorX() != structureMirror : random.nextBoolean());
                AxisAlignedTransform2D strucTransform = AxisAlignedTransform2D.transform(strucRotation, strucMirror);

                int[] strucSize = structureInfo.structureBoundingBox();
                BlockCoord coord = transform.apply(structureShift, new int[]{1, 1, 1}).add(xCoord, yCoord, zCoord).subtract(transform.apply(new BlockCoord(0, 0, 0), strucSize));

                WorldGenStructures.generateStructureWithNotifications(structureInfo, world, random, coord, strucTransform, layer, false);
            }
        }
    }

    public static List<String> generatorsFromNBT(NBTTagCompound nbtTagCompound)
    {
        List<String> list = new ArrayList<>();

        NBTTagList structureNBTList = nbtTagCompound.getTagList("structures", Constants.NBT.TAG_STRING);
        for (int i = 0; i < structureNBTList.tagCount(); i++)
        {
            list.add(structureNBTList.getStringTagAt(i));
        }

        return list;
    }

//
//    @Override
//    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
//    {
//        readStructureDataFromNBT(pkt.func_148857_g());
//    }
//
//    @Override
//    public Packet getDescriptionPacket()
//    {
//        NBTTagCompound nbttagcompound = new NBTTagCompound();
//        this.writeStructureDataToNBT(nbttagcompound);
//        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbttagcompound);
//    }
}
