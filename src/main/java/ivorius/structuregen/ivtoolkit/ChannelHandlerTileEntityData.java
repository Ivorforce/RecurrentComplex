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

package ivorius.structuregen.ivtoolkit;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/**
 * Created by lukas on 13.04.14.
 */
@ChannelHandler.Sharable
public class ChannelHandlerTileEntityData extends SimpleChannelInboundHandler<FMLProxyPacket>
{
    public final String packetChannel;

    public ChannelHandlerTileEntityData(String packetChannel)
    {
        this.packetChannel = packetChannel;
    }

    /**
     * Sends an update packet for the entity to every watching player's client, if the current active side is the server.
     * Only use this method for complex updates; if you have a primitive to sync, use the entity's data watcher instead.
     *
     * @param tileEntity The tile entity, both extending TileEntity and implementing IEntityUpdateData.
     * @param context    The update context for the packet. Will be passed in writeUpdateData and readUpdateData. Keep in mind you can also add your own additional context information to the buffer.
     */
    public <UpdatableTE extends TileEntity & ITileEntityUpdateData> void sendUpdatePacketSafe(UpdatableTE tileEntity, String context)
    {
        if (!tileEntity.getWorldObj().isRemote && tileEntity.getWorldObj() instanceof WorldServer)
        {
            sendUpdatePacket(tileEntity, context);
        }
    }

    /**
     * Sends an update packet for the tile entity to every watching player's client.
     * Do not invoke this method directly, always use {@link #sendUpdatePacketSafe(net.minecraft.tileentity.TileEntity, String)} instead.
     *
     * @param tileEntity The tile entity, both extending TileEntity and implementing IEntityUpdateData.
     * @param context    The update context for the packet. Will be passed in writeUpdateData and readUpdateData. Keep in mind you can also add your own additional context information to the buffer.
     */
    public <UpdatableTE extends TileEntity & ITileEntityUpdateData> void sendUpdatePacket(UpdatableTE tileEntity, String context)
    {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(tileEntity.xCoord);
        buffer.writeInt(tileEntity.yCoord);
        buffer.writeInt(tileEntity.zCoord);
        ByteBufUtils.writeUTF8String(buffer, Block.blockRegistry.getNameForObject(tileEntity.getBlockType()));
        ByteBufUtils.writeUTF8String(buffer, context);
        tileEntity.writeUpdateData(buffer, context);

        FMLProxyPacket packet = new FMLProxyPacket(buffer, packetChannel);
        IvTileEntityHelper.sendToPlayersWatchingChunk(tileEntity.getWorldObj(), tileEntity.xCoord >> 4, tileEntity.zCoord >> 4, packet);
    }

    public <UpdatableTE extends TileEntity & ITileEntityUpdateData> void sendPacketToServerSafe(UpdatableTE tileEntity, String context)
    {
        if (tileEntity.getWorldObj().isRemote && tileEntity.getWorldObj() instanceof WorldClient)
        {
            sendPacketToServer(tileEntity, context);
        }
    }

    public <UpdatableTE extends TileEntity & ITileEntityUpdateData> void sendPacketToServer(UpdatableTE tileEntity, String context)
    {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(tileEntity.xCoord);
        buffer.writeInt(tileEntity.yCoord);
        buffer.writeInt(tileEntity.zCoord);
        ByteBufUtils.writeUTF8String(buffer, Block.blockRegistry.getNameForObject(tileEntity.getBlockType()));
        ByteBufUtils.writeUTF8String(buffer, context);
        buffer.writeInt(tileEntity.getWorldObj().provider.dimensionId);
        tileEntity.writeUpdateData(buffer, context);

        FMLProxyPacket packet = new FMLProxyPacket(buffer, packetChannel);
        Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(packet);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FMLProxyPacket msg) throws Exception
    {
        Side channelSide = ctx.channel().attr(NetworkRegistry.CHANNEL_SOURCE).get();

        ByteBuf buffer = msg.payload();
        int xCoord = buffer.readInt();
        int yCoord = buffer.readInt();
        int zCoord = buffer.readInt();
        Block blockType = Block.getBlockFromName(ByteBufUtils.readUTF8String(buffer));
        String context = ByteBufUtils.readUTF8String(buffer);

        World world;

        if (channelSide == Side.CLIENT)
        {
            world = Minecraft.getMinecraft().theWorld;
        }
        else
        {
            int dimension = buffer.readInt();
            world = MinecraftServer.getServer().worldServerForDimension(dimension);
        }

        TileEntity entity = world.getTileEntity(xCoord, yCoord, zCoord);

        if (entity instanceof ITileEntityUpdateData && entity.getBlockType() == blockType)
        {
            ((ITileEntityUpdateData) entity).readUpdateData(buffer, context);
        }
    }
}
