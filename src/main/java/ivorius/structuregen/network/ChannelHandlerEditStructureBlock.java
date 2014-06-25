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
import ivorius.structuregen.blocks.TileEntityStructureGenerator;
import ivorius.structuregen.gui.editstructureblock.GuiEditStructureBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Created by lukas on 13.04.14.
 */
@ChannelHandler.Sharable
public class ChannelHandlerEditStructureBlock extends SimpleChannelInboundHandler<FMLProxyPacket>
{
    public final String packetChannel;

    public ChannelHandlerEditStructureBlock(String packetChannel)
    {
        this.packetChannel = packetChannel;
    }

    public void sendBeginEdit(EntityPlayerMP player, TileEntityStructureGenerator structureGenerator)
    {
        ByteBuf buffer = Unpooled.buffer();
        NBTTagCompound compound = new NBTTagCompound();
        structureGenerator.writeStructureDataToNBT(compound);

        buffer.writeInt(structureGenerator.xCoord);
        buffer.writeInt(structureGenerator.yCoord);
        buffer.writeInt(structureGenerator.zCoord);
        ByteBufUtils.writeTag(buffer, compound);

        FMLProxyPacket packet = new FMLProxyPacket(buffer, packetChannel);
        player.playerNetServerHandler.sendPacket(packet);
    }

    public void sendSaveEdit(EntityClientPlayerMP player, TileEntityStructureGenerator structureGenerator)
    {
        ByteBuf buffer = Unpooled.buffer();
        NBTTagCompound compound = new NBTTagCompound();
        structureGenerator.writeStructureDataToNBT(compound);

        buffer.writeInt(structureGenerator.getWorldObj().provider.dimensionId);
        buffer.writeInt(structureGenerator.xCoord);
        buffer.writeInt(structureGenerator.yCoord);
        buffer.writeInt(structureGenerator.zCoord);
        ByteBufUtils.writeTag(buffer, compound);

        FMLProxyPacket packet = new FMLProxyPacket(buffer, packetChannel);
        player.sendQueue.addToSendQueue(packet);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FMLProxyPacket msg) throws Exception
    {
        Side channelSide = ctx.channel().attr(NetworkRegistry.CHANNEL_SOURCE).get();
//        INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();

        ByteBuf buffer = msg.payload();

        if (channelSide == Side.CLIENT)
        {
            int x = buffer.readInt();
            int y = buffer.readInt();
            int z = buffer.readInt();
            NBTTagCompound compound = ByteBufUtils.readTag(buffer);

            TileEntity tileEntity = Minecraft.getMinecraft().theWorld.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityStructureGenerator)
            {
                TileEntityStructureGenerator tileEntityStructureGenerator = ((TileEntityStructureGenerator) tileEntity);

                tileEntityStructureGenerator.readStructureDataFromNBT(compound);
                Minecraft.getMinecraft().displayGuiScreen(new GuiEditStructureBlock(tileEntityStructureGenerator));
            }
        }
        else
        {
            int dimension = buffer.readInt();
            int x = buffer.readInt();
            int y = buffer.readInt();
            int z = buffer.readInt();
            NBTTagCompound compound = ByteBufUtils.readTag(buffer);

            World world = MinecraftServer.getServer().worldServerForDimension(dimension);
            if (world != null)
            {
                TileEntity tileEntity = world.getTileEntity(x, y, z);

                if (tileEntity instanceof TileEntityStructureGenerator)
                {
                    ((TileEntityStructureGenerator) tileEntity).readStructureDataFromNBT(compound);
                    tileEntity.markDirty();
                    world.markBlockForUpdate(x, y, z);
                }
            }
        }
    }
}
