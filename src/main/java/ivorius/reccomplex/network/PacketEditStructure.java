/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.GenericStructureInfo;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditStructure implements IMessage
{
    private GenericStructureInfo structureInfo;
    private String key;
    private boolean saveAsActive;

    public PacketEditStructure()
    {
    }

    public PacketEditStructure(GenericStructureInfo structureInfo, String key, boolean saveAsActive)
    {
        this.structureInfo = structureInfo;
        this.key = key;
        this.saveAsActive = saveAsActive;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
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

    @Override
    public void fromBytes(ByteBuf buf)
    {
        key = ByteBufUtils.readUTF8String(buf);
        String json = ByteBufUtils.readUTF8String(buf);
        structureInfo = StructureRegistry.createStructureFromJSON(json);
        saveAsActive = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, key);
        ByteBufUtils.writeUTF8String(buf, StructureRegistry.createJSONFromStructure(structureInfo));
        buf.writeBoolean(saveAsActive);
    }
}
