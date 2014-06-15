/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
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
import ivorius.structuregen.entities.StructureEntityInfo;
import ivorius.structuregen.gui.editstructure.GuiEditGenericStructure;
import ivorius.structuregen.worldgen.StructureHandler;
import ivorius.structuregen.worldgen.StructureSaveHandler;
import ivorius.structuregen.worldgen.genericStructures.GenericStructureInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.ChatComponentTranslation;

/**
 * Created by lukas on 13.04.14.
 */
@ChannelHandler.Sharable
public class ChannelHandlerEditStructure extends SimpleChannelInboundHandler<FMLProxyPacket>
{
    public final String packetChannel;

    public ChannelHandlerEditStructure(String packetChannel)
    {
        this.packetChannel = packetChannel;
    }

    public void sendBeginEdit(EntityPlayerMP player, GenericStructureInfo structure, String key)
    {
        StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(player);
        if (structureEntityInfo != null)
        {
            structureEntityInfo.setCachedExportStructureBlockDataNBT(structure.worldDataCompound);
            structure.worldDataCompound = null; // Do not send world data
        }

        ByteBuf buffer = Unpooled.buffer();
        ByteBufUtils.writeUTF8String(buffer, key);
        ByteBufUtils.writeUTF8String(buffer, StructureHandler.createJSONFromStructure(structure));

        FMLProxyPacket packet = new FMLProxyPacket(buffer, packetChannel);
        player.playerNetServerHandler.sendPacket(packet);

        // Restore state, jic
        if (structureEntityInfo != null)
        {
            structure.worldDataCompound = structureEntityInfo.getCachedExportStructureBlockDataNBT();
        }
    }

    public void sendSaveEdit(EntityClientPlayerMP player, GenericStructureInfo structure, String key)
    {
        ByteBuf buffer = Unpooled.buffer();
        ByteBufUtils.writeUTF8String(buffer, key);
        String json = StructureHandler.createJSONFromStructure(structure);
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
        GenericStructureInfo structure = StructureHandler.createStructureFromJSON(json);

        if (channelSide == Side.CLIENT)
        {
//            NetHandlerPlayClient netHandlerPlayClient = (NetHandlerPlayClient) netHandler;
            Minecraft.getMinecraft().displayGuiScreen(new GuiEditGenericStructure(key, structure));
        }
        else
        {
            NetHandlerPlayServer netHandlerPlayServer = (NetHandlerPlayServer) netHandler;
            EntityPlayerMP player = netHandlerPlayServer.playerEntity;
            StructureEntityInfo structureEntityInfo = StructureEntityInfo.getStructureEntityInfo(player);

            if (structureEntityInfo != null)
            {
                structure.worldDataCompound = structureEntityInfo.getCachedExportStructureBlockDataNBT();
            }

            if (!StructureSaveHandler.saveGenericStructure(structure, key))
            {
                player.addChatMessage(new ChatComponentTranslation("commands.strucExport.failure", key));
            }
            else
            {
                player.addChatMessage(new ChatComponentTranslation("commands.strucExport.success", key));
                StructureSaveHandler.reloadAllCustomStructures();
            }
        }
    }
}
