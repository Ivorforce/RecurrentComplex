/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import ivorius.reccomplex.worldgen.StructureRegistry;
import ivorius.reccomplex.worldgen.genericStructures.GenericStructureInfo;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditStructure implements IMessage
{
    private String key;
    private GenericStructureInfo structureInfo;

    public PacketEditStructure()
    {
    }

    public PacketEditStructure(String key, GenericStructureInfo structureInfo)
    {
        this.key = key;
        this.structureInfo = structureInfo;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
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
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, key);
        ByteBufUtils.writeUTF8String(buf, StructureRegistry.createJSONFromStructure(structureInfo));
    }
}
