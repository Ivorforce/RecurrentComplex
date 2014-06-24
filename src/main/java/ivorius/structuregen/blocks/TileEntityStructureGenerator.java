/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.blocks;

import ivorius.structuregen.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.structuregen.ivtoolkit.blocks.BlockCoord;
import ivorius.structuregen.ivtoolkit.tools.IvCollections;
import ivorius.structuregen.worldgen.StructureHandler;
import ivorius.structuregen.worldgen.StructureInfo;
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
    }

    @Override
    public void generate(World world, Random random, AxisAlignedTransform2D transform, int layer)
    {
        world.setBlockToAir(xCoord, yCoord, zCoord);

        if (structureNames.size() > 0)
        {
            String structure = structureNames.get(random.nextInt(structureNames.size()));
            StructureInfo structureInfo = StructureHandler.getStructure(structure);

            if (structureInfo != null)
            {
                structureInfo.generate(world, random, new BlockCoord(xCoord + structureShift.x, yCoord + structureShift.y, zCoord + structureShift.z), transform, layer);
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
