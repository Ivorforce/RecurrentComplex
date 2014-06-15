/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ivorius.structuregen.gui.editinventorygen.GuiEditInventoryGen;
import ivorius.structuregen.items.ItemInventoryGenerationTag;
import ivorius.structuregen.worldgen.inventory.GenericInventoryGenerator;
import ivorius.structuregen.worldgen.inventory.InventoryGenerationHandler;
import ivorius.structuregen.worldgen.inventory.InventoryGeneratorSaveHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;

/**
 * Created by lukas on 13.04.14.
 */
@ChannelHandler.Sharable
public class ChannelHandlerEditInventoryGenerator extends SimpleChannelInboundHandler<FMLProxyPacket>
{
    public final String packetChannel;

    public ChannelHandlerEditInventoryGenerator(String packetChannel)
    {
        this.packetChannel = packetChannel;
    }

    public void sendBeginEdit(EntityPlayerMP player, GenericInventoryGenerator generator, String key)
    {
        ByteBuf buffer = Unpooled.buffer();
        ByteBufUtils.writeUTF8String(buffer, key);
        String json = InventoryGenerationHandler.createJSONFromInventoryGenerator(generator);
        ByteBufUtils.writeUTF8String(buffer, json);

        FMLProxyPacket packet = new FMLProxyPacket(buffer, packetChannel);
        player.playerNetServerHandler.sendPacket(packet);
    }

    public void sendSaveEdit(EntityClientPlayerMP player, GenericInventoryGenerator generator, String key)
    {
        ByteBuf buffer = Unpooled.buffer();
        ByteBufUtils.writeUTF8String(buffer, key);
        String json = InventoryGenerationHandler.createJSONFromInventoryGenerator(generator);
        ByteBufUtils.writeUTF8String(buffer, json);

        FMLProxyPacket packet = new FMLProxyPacket(buffer, packetChannel);
        player.sendQueue.addToSendQueue(packet);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FMLProxyPacket msg) throws Exception
    {
        Side channelSide = ctx.channel().attr(NetworkRegistry.CHANNEL_SOURCE).get();
        INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();

        ByteBuf buffer = msg.payload();
        String key = ByteBufUtils.readUTF8String(buffer);
        String json = ByteBufUtils.readUTF8String(buffer);
        GenericInventoryGenerator generator = InventoryGenerationHandler.createInventoryGeneratorFromJSON(json);

        if (channelSide == Side.CLIENT)
        {
            Minecraft.getMinecraft().displayGuiScreen(new GuiEditInventoryGen(Minecraft.getMinecraft().thePlayer, generator, key));
        }
        else
        {
            NetHandlerPlayServer playServer = (NetHandlerPlayServer) netHandler;

            InventoryGeneratorSaveHandler.saveInventoryGenerator(generator, key);
            InventoryGeneratorSaveHandler.reloadAllCustomInventoryGenerators();

            ItemStack heldItem = playServer.playerEntity.getHeldItem();
            if (heldItem != null && heldItem.getItem() instanceof ItemInventoryGenerationTag)
            {
                ItemInventoryGenerationTag.setItemStackGeneratorKey(heldItem, key);
            }
        }
    }
}
