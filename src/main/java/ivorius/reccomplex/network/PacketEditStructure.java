/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import io.netty.buffer.ByteBuf;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.StructureSaveHandler;
import ivorius.reccomplex.utils.SaveDirectoryData;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditStructure implements IMessage
{
    private GenericStructure structureInfo;
    private String structureID;

    private SaveDirectoryData saveDirectoryData;

    public PacketEditStructure()
    {
    }

    public PacketEditStructure(GenericStructure structureInfo, String structureID, SaveDirectoryData saveDirectoryData)
    {
        this.structureInfo = structureInfo;
        this.structureID = structureID;
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

    @Override
    public void fromBytes(ByteBuf buf)
    {
        structureID = ByteBufUtils.readUTF8String(buf);
        structureInfo = StructureSaveHandler.INSTANCE.fromJSON(ByteBufUtils.readUTF8String(buf));
        saveDirectoryData = SaveDirectoryData.readFrom(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, structureID);
        ByteBufUtils.writeUTF8String(buf, StructureSaveHandler.INSTANCE.toJSON(structureInfo));
        saveDirectoryData.writeTo(buf);
    }
}
