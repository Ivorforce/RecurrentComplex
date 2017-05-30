/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
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
public class PacketSaveStructure implements IMessage
{
    private GenericStructure structureInfo;
    private String structureID;

    private SaveDirectoryData.Result saveDirectoryDataResult;

    public PacketSaveStructure()
    {
    }

    public PacketSaveStructure(GenericStructure structureInfo, String structureID, SaveDirectoryData.Result saveDirectoryDataResult)
    {
        this.structureInfo = structureInfo;
        this.structureID = structureID;
        this.saveDirectoryDataResult = saveDirectoryDataResult;
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

    public SaveDirectoryData.Result getSaveDirectoryDataResult()
    {
        return saveDirectoryDataResult;
    }

    public void setSaveDirectoryDataResult(SaveDirectoryData.Result saveDirectoryDataResult)
    {
        this.saveDirectoryDataResult = saveDirectoryDataResult;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        structureID = ByteBufUtils.readUTF8String(buf);
        structureInfo = StructureSaveHandler.INSTANCE.fromJSON(ByteBufUtils.readUTF8String(buf), null);
        saveDirectoryDataResult = SaveDirectoryData.Result.readFrom(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, structureID);
        ByteBufUtils.writeUTF8String(buf, StructureSaveHandler.INSTANCE.toJSON(structureInfo));
        saveDirectoryDataResult.writeTo(buf);
    }
}
