/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import io.netty.buffer.ByteBuf;
import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.StructureSaveHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditStructure implements IMessage
{
    protected GenericStructure structureInfo;
    protected String structureID;

    protected BlockPos lowerCoord;

    protected SaveDirectoryData saveDirectoryData;

    public PacketEditStructure()
    {
    }

    public PacketEditStructure(GenericStructure structureInfo, String structureID, BlockPos lowerCoord, SaveDirectoryData saveDirectoryData)
    {
        this.structureInfo = structureInfo;
        this.structureID = structureID;
        this.lowerCoord = lowerCoord;
        this.saveDirectoryData = saveDirectoryData;
    }

    public String getStructureID()
    {
        return structureID;
    }

    public void setStructureID(String structureID)
    {
        this.structureID = structureID;
    }

    public GenericStructure getStructureInfo()
    {
        return structureInfo;
    }

    public void setStructureInfo(GenericStructure structureInfo)
    {
        this.structureInfo = structureInfo;
    }

    public SaveDirectoryData getSaveDirectoryData()
    {
        return saveDirectoryData;
    }

    public void setSaveDirectoryData(SaveDirectoryData saveDirectoryData)
    {
        this.saveDirectoryData = saveDirectoryData;
    }

    public BlockPos getLowerCoord()
    {
        return lowerCoord;
    }

    public void setLowerCoord(BlockPos lowerCoord)
    {
        this.lowerCoord = lowerCoord;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        structureID = ByteBufUtils.readUTF8String(buf);
        structureInfo = StructureSaveHandler.INSTANCE.fromJSON(ByteBufUtils.readUTF8String(buf), null);
        lowerCoord = BlockPositions.readFromBuffer(buf);
        saveDirectoryData = SaveDirectoryData.readFrom(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, structureID);
        ByteBufUtils.writeUTF8String(buf, StructureSaveHandler.INSTANCE.toJSON(structureInfo));
        BlockPositions.writeToBuffer(lowerCoord, buf);
        saveDirectoryData.writeTo(buf);
    }
}
