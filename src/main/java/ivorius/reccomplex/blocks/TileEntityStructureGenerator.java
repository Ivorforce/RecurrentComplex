/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import com.google.common.base.Function;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.IvCollections;
import ivorius.reccomplex.gui.editstructureblock.GuiEditStructureBlock;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.gentypes.StructureListGenerationInfo;
import ivorius.reccomplex.utils.Directions;
import ivorius.reccomplex.utils.WeightedSelector;
import ivorius.reccomplex.worldgen.StructureGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by lukas on 06.06.14.
 */
public class TileEntityStructureGenerator extends TileEntity implements GeneratingTileEntity, TileEntityWithGUI
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
        super.readFromNBT(nbtTagCompound);

        readSyncedNBT(nbtTagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        super.writeToNBT(nbtTagCompound);

        writeSyncedNBT(nbtTagCompound);
    }

    @Override
    public void generate(World world, Random random, AxisAlignedTransform2D transform, int layer)
    {
        world.setBlockToAir(xCoord, yCoord, zCoord);

        if (simpleMode)
        {
            if (structureNames.size() > 0)
            {
                String structure = structureNames.get(random.nextInt(structureNames.size()));
                StructureInfo structureInfo = StructureRegistry.getStructure(structure);

                if (structureInfo != null)
                {
                    int rotations = structureInfo.isRotatable() ? (structureRotation != null ? transform.getRotation() + structureRotation : random.nextInt(4)) : 0;
                    boolean mirrorX = structureInfo.isMirrorable() && (structureMirror != null ? transform.isMirrorX() != structureMirror : random.nextBoolean());
                    AxisAlignedTransform2D strucTransform = AxisAlignedTransform2D.transform(rotations, mirrorX);

                    int[] strucSize = structureInfo.structureBoundingBox();
                    BlockCoord coord = transform.apply(structureShift, new int[]{1, 1, 1}).add(xCoord, yCoord, zCoord).subtract(transform.apply(new BlockCoord(0, 0, 0), strucSize));

                    StructureGenerator.generateStructureWithNotifications(structureInfo, world, random, coord, strucTransform, layer, false);
                }
            }
        }
        else
        {
            Collection<Pair<StructureInfo, StructureListGenerationInfo>> generationInfos = StructureRegistry.getStructuresInList(structureListID, front);

            if (generationInfos.size() > 0)
            {
                Pair<StructureInfo, StructureListGenerationInfo> pair = WeightedSelector.select(random, generationInfos, new Function<Pair<StructureInfo, StructureListGenerationInfo>, Double>()
                {
                    @Nullable
                    @Override
                    public Double apply(Pair<StructureInfo, StructureListGenerationInfo> input)
                    {
                        return input.getRight().getWeight();
                    }
                });
                StructureInfo structureInfo = pair.getLeft();
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
                BlockCoord coord = transform.apply(structureShift.add(generationInfo.shiftX, generationInfo.shiftY, generationInfo.shiftZ), new int[]{1, 1, 1})
                        .add(xCoord, yCoord, zCoord)
                        .subtract(transform.apply(new BlockCoord(0, 0, 0), strucSize));

                StructureGenerator.generateStructureWithNotifications(structureInfo, world, random, coord, strucTransform, layer, false);
            }
        }
    }

    @Override
    public void writeSyncedNBT(NBTTagCompound compound)
    {
        BlockCoord.writeCoordToNBT("structureShift", structureShift, compound);

        compound.setBoolean("simpleMode", simpleMode);

        compound.setString("structureListID", structureListID);
        if (front != null)
            compound.setString("front", Directions.serialize(front));

        NBTTagList structureNBTList = new NBTTagList();
        for (String struc : structureNames)
            structureNBTList.appendTag(new NBTTagString(struc));
        compound.setTag("structures", structureNBTList);

        if (structureRotation != null)
            compound.setInteger("structureRotation", structureRotation);

        if (structureMirror != null)
            compound.setBoolean("structureMirror", structureMirror);
    }

    @Override
    public void readSyncedNBT(NBTTagCompound compound)
    {
        structureShift = BlockCoord.readCoordFromNBT("structureShift", compound);

        simpleMode = !compound.hasKey("simpleMode", Constants.NBT.TAG_BYTE)
                || compound.getBoolean("simpleMode"); // Legacy

        structureListID = compound.getString("structureListID");
        front = compound.hasKey("front", Constants.NBT.TAG_STRING) ? Directions.deserialize(compound.getString("front")) : null;

        List<String> structureList = new ArrayList<>();
        NBTTagList structureNBTList = compound.getTagList("structures", Constants.NBT.TAG_STRING);
        for (int i = 0; i < structureNBTList.tagCount(); i++)
            structureList.add(structureNBTList.getStringTagAt(i));
        IvCollections.setContentsOfList(structureNames, structureList);

        structureRotation = compound.hasKey("structureRotation") ? compound.getInteger("structureRotation") : null;
        structureMirror = compound.hasKey("structureMirror") ? compound.getBoolean("structureMirror") : null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void openEditGUI()
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiEditStructureBlock(this));
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
