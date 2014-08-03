package ivorius.ivtoolkit.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.inventory.Container;
import net.minecraft.network.NetHandlerPlayServer;

/**
 * Created by lukas on 01.07.14.
 */
public class PacketGuiAction implements IMessage
{
    private String context;
    private ByteBuf payload;

    public PacketGuiAction()
    {
    }

    public PacketGuiAction(String context, ByteBuf payload)
    {
        this.context = context;
        this.payload = payload;
    }

    public static PacketGuiAction packetGuiAction(String context, Number... args)
    {
        ByteBuf payload = Unpooled.buffer();

        for (Number num : args)
        {
            IvPacketHelper.writeNumber(payload, num);
        }

        return new PacketGuiAction(context, payload);
    }

    public String getContext()
    {
        return context;
    }

    public void setContext(String context)
    {
        this.context = context;
    }

    public ByteBuf getPayload()
    {
        return payload;
    }

    public void setPayload(ByteBuf payload)
    {
        this.payload = payload;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        context = ByteBufUtils.readUTF8String(buf);
        payload = IvPacketHelper.readByteBuffer(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, context);
        IvPacketHelper.writeByteBuffer(buf, payload);
    }

    public static interface ActionHandler
    {
        void handleAction(String context, ByteBuf buffer);
    }
}
