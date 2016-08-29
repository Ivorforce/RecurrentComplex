/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import io.netty.buffer.ByteBuf;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketSaveStructure implements IMessage
{
    private GenericStructureInfo structureInfo;
    private String structureID;

    private boolean saveAsActive;
    private boolean deleteOther;

    public PacketSaveStructure()
    {
    }

    public PacketSaveStructure(GenericStructureInfo structureInfo, String structureID, boolean saveAsActive, boolean deleteOther)
    {
        this.structureInfo = structureInfo;
        this.structureID = structureID;
        this.saveAsActive = saveAsActive;
        this.saveAsActive = saveAsActive;
        this.deleteOther = deleteOther;
    }

    public String getStructureID()
    {
        return structureID;
    }

    public void setStructureID(String structureID)
    {
        this.structureID = structureID;
    }

    public boolean isSaveAsActive()
    {
        return saveAsActive;
    }

    public void setSaveAsActive(boolean saveAsActive)
    {
        this.saveAsActive = saveAsActive;
    }

    public GenericStructureInfo getStructureInfo()
    {
        return structureInfo;
    }

    public void setStructureInfo(GenericStructureInfo structureInfo)
    {
        this.structureInfo = structureInfo;
    }

    public boolean isDeleteOther()
    {
        return deleteOther;
    }

    public void setDeleteOther(boolean deleteOther)
    {
        this.deleteOther = deleteOther;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        structureID = ByteBufUtils.readUTF8String(buf);
        structureInfo = StructureRegistry.INSTANCE.createStructureFromJSON(ByteBufUtils.readUTF8String(buf));
        saveAsActive = buf.readBoolean();
        deleteOther = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, structureID);
        ByteBufUtils.writeUTF8String(buf, StructureRegistry.INSTANCE.createJSONFromStructure(structureInfo));
        buf.writeBoolean(saveAsActive);
        buf.writeBoolean(deleteOther);
    }
}
