/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.ivtoolkit.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.network.NetHandlerPlayServer;

/**
 * Created by lukas on 13.04.14.
 */
@ChannelHandler.Sharable
public class ChannelHandlerGuiAction extends SimpleChannelInboundHandler<FMLProxyPacket>
{
    public final String packetChannel;

    public ChannelHandlerGuiAction(String packetChannel)
    {
        this.packetChannel = packetChannel;
    }

    public static void writeNumber(ByteBuf buffer, Number number)
    {
        if (number instanceof Byte)
        {
            buffer.writeByte((Byte) number);
        }
        else if (number instanceof Double)
        {
            buffer.writeDouble((Double) number);
        }
        else if (number instanceof Float)
        {
            buffer.writeFloat((Float) number);
        }
        else if (number instanceof Integer)
        {
            buffer.writeInt((Integer) number);
        }
        else if (number instanceof Long)
        {
            buffer.writeLong((Long) number);
        }
        else if (number instanceof Short)
        {
            buffer.writeShort((Short) number);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    public void sendActionToServer(String context, Number... args)
    {
        ByteBuf buffer = Unpooled.buffer();
        ByteBufUtils.writeUTF8String(buffer, context);

        for (Number num : args)
        {
            writeNumber(buffer, num);
        }

        FMLProxyPacket packet = new FMLProxyPacket(buffer, packetChannel);
        Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(packet);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FMLProxyPacket msg) throws Exception
    {
        Side channelSide = ctx.channel().attr(NetworkRegistry.CHANNEL_SOURCE).get();

        ByteBuf buffer = msg.payload();
        String context = ByteBufUtils.readUTF8String(buffer);

        if (channelSide == Side.SERVER)
        {
            NetHandlerPlayServer netHandler = (NetHandlerPlayServer) ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
            Container container = netHandler.playerEntity.openContainer;
            if (container instanceof IGuiActionHandler)
            {
                ((IGuiActionHandler) container).handleAction(context, buffer);
            }
        }
    }
}
