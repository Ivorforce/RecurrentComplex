/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import io.netty.buffer.ByteBuf;
import ivorius.ivtoolkit.network.IvPacketHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketInspectEntity implements IMessage
{
    private NBTTagCompound data;
    private UUID uuid;
    private String name;

    public PacketInspectEntity()
    {
    }

    public PacketInspectEntity(NBTTagCompound data, UUID uuid, String name)
    {
        this.data = data;
        this.uuid = uuid;
        this.name = name;
    }

    public NBTTagCompound getData()
    {
        return data;
    }

    public void setData(NBTTagCompound data)
    {
        this.data = data;
    }

    public UUID getUuid()
    {
        return uuid;
    }

    public void setUuid(UUID uuid)
    {
        this.uuid = uuid;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        data = IvPacketHelper.maybeRead(buf, null, () -> ByteBufUtils.readTag(buf));
        uuid = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        name = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        IvPacketHelper.maybeWrite(buf, data, () -> ByteBufUtils.writeTag(buf, data));
        ByteBufUtils.writeUTF8String(buf, uuid.toString());
        ByteBufUtils.writeUTF8String(buf, name);
    }
}
