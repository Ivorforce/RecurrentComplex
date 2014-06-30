/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ivorius.reccomplex.blocks.TileEntityMazeGenerator;
import ivorius.reccomplex.gui.editmazeblock.GuiEditMazeBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Created by lukas on 13.04.14.
 */
@ChannelHandler.Sharable
public class ChannelHandlerEditMazeBlock extends SimpleChannelInboundHandler<FMLProxyPacket>
{
    public final String packetChannel;

    public ChannelHandlerEditMazeBlock(String packetChannel)
    {
        this.packetChannel = packetChannel;
    }

    public void sendBeginEdit(EntityPlayerMP player, TileEntityMazeGenerator mazeGenerator)
    {
        ByteBuf buffer = Unpooled.buffer();
        NBTTagCompound compound = new NBTTagCompound();
        mazeGenerator.writeMazeDataToNBT(compound);

        buffer.writeInt(mazeGenerator.xCoord);
        buffer.writeInt(mazeGenerator.yCoord);
        buffer.writeInt(mazeGenerator.zCoord);
        ByteBufUtils.writeTag(buffer, compound);

        FMLProxyPacket packet = new FMLProxyPacket(buffer, packetChannel);
        player.playerNetServerHandler.sendPacket(packet);
    }

    public void sendSaveEdit(EntityClientPlayerMP player, TileEntityMazeGenerator mazeGenerator)
    {
        ByteBuf buffer = Unpooled.buffer();
        NBTTagCompound compound = new NBTTagCompound();
        mazeGenerator.writeMazeDataToNBT(compound);

        buffer.writeInt(mazeGenerator.getWorldObj().provider.dimensionId);
        buffer.writeInt(mazeGenerator.xCoord);
        buffer.writeInt(mazeGenerator.yCoord);
        buffer.writeInt(mazeGenerator.zCoord);
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
            if (tileEntity instanceof TileEntityMazeGenerator)
            {
                TileEntityMazeGenerator tileEntityMazeGenerator = ((TileEntityMazeGenerator) tileEntity);

                tileEntityMazeGenerator.readMazeDataFromNBT(compound);
                Minecraft.getMinecraft().displayGuiScreen(new GuiEditMazeBlock(tileEntityMazeGenerator));
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

                if (tileEntity instanceof TileEntityMazeGenerator)
                {
                    ((TileEntityMazeGenerator) tileEntity).readMazeDataFromNBT(compound);
                    tileEntity.markDirty();
                    world.markBlockForUpdate(x, y, z);
                }
            }
        }
    }
}
