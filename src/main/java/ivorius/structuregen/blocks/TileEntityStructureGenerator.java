/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.blocks;

import ivorius.structuregen.worldgen.StructureHandler;
import ivorius.structuregen.worldgen.StructureInfo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 06.06.14.
 */
public class TileEntityStructureGenerator extends TileEntity implements GeneratingTileEntity
{
    private List<String> structureNames = new ArrayList<>();

    private int structureShiftX;
    private int structureShiftY;
    private int structureShiftZ;

    public List<String> getStructureNames()
    {
        return structureNames;
    }

    public void setStructureNames(List<String> structureNames)
    {
        this.structureNames.clear();
        this.structureNames.addAll(structureNames);
    }

    public int getStructureShiftX()
    {
        return structureShiftX;
    }

    public void setStructureShiftX(int structureShiftX)
    {
        this.structureShiftX = structureShiftX;
    }

    public int getStructureShiftY()
    {
        return structureShiftY;
    }

    public void setStructureShiftY(int structureShiftY)
    {
        this.structureShiftY = structureShiftY;
    }

    public int getStructureShiftZ()
    {
        return structureShiftZ;
    }

    public void setStructureShiftZ(int structureShiftZ)
    {
        this.structureShiftZ = structureShiftZ;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        super.readFromNBT(nbtTagCompound);

        readStructureDataFromNBT(nbtTagCompound);
    }

    public void readStructureDataFromNBT(NBTTagCompound nbtTagCompound)
    {
        structureNames.clear();
        structureNames.addAll(generatorsFromNBT(nbtTagCompound));

        structureShiftX = nbtTagCompound.getInteger("structureShiftX");
        structureShiftY = nbtTagCompound.getInteger("structureShiftY");
        structureShiftZ = nbtTagCompound.getInteger("structureShiftZ");
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

        nbtTagCompound.setInteger("structureShiftX", structureShiftX);
        nbtTagCompound.setInteger("structureShiftY", structureShiftY);
        nbtTagCompound.setInteger("structureShiftZ", structureShiftZ);
    }

    @Override
    public void generate(World world, Random random, int layer)
    {
        if (structureNames.size() > 0)
        {
            String structure = structureNames.get(random.nextInt(structureNames.size()));
            StructureInfo structureInfo = StructureHandler.getStructure(structure);

            if (structureInfo != null)
            {
                structureInfo.generate(world, random, xCoord + structureShiftX, yCoord + structureShiftY, zCoord + structureShiftZ, false, layer);
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
