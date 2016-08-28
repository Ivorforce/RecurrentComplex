/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import io.netty.buffer.ByteBuf;
import ivorius.ivtoolkit.network.IvPacketHelper;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Created by lukas on 28.08.16.
 */
public class PacketOpenGui implements IMessage
{
    public int windowId;
    public String modId;
    public int modGuiId;
    public ByteBuf data;

    public PacketOpenGui()
    {
    }

    public PacketOpenGui(int windowId, String modId, int modGuiId, ByteBuf data)
    {
        this.windowId = windowId;
        this.modId = modId;
        this.modGuiId = modGuiId;
        this.data = data;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(windowId);
        ByteBufUtils.writeUTF8String(buf, modId);
        buf.writeInt(modGuiId);
        IvPacketHelper.writeByteBuffer(buf, data);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        windowId = buf.readInt();
        modId = ByteBufUtils.readUTF8String(buf);
        modGuiId = buf.readInt();
        data = IvPacketHelper.readByteBuffer(buf);
    }
}
