/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import io.netty.buffer.ByteBuf;
import ivorius.ivtoolkit.blocks.BlockPositions;
import ivorius.ivtoolkit.network.IvPacketHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
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

    public PacketInspectEntity()
    {
    }

    public PacketInspectEntity(NBTTagCompound data, UUID uuid)
    {
        this.data = data;
        this.uuid = uuid;
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

    @Override
    public void fromBytes(ByteBuf buf)
    {
        data = IvPacketHelper.maybeRead(buf, null, () -> ByteBufUtils.readTag(buf));
        uuid = UUID.fromString(ByteBufUtils.readUTF8String(buf));
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        IvPacketHelper.maybeWrite(buf, data, () -> ByteBufUtils.writeTag(buf, data));
        ByteBufUtils.writeUTF8String(buf, uuid.toString());
    }
}
