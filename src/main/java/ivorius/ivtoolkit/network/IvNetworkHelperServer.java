package ivorius.ivtoolkit.network;

import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import io.netty.channel.Channel;
import ivorius.reccomplex.entities.StructureEntityInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IExtendedEntityProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 02.07.14.
 */
public class IvNetworkHelperServer
{
    public static void sendEEPUpdatePacket(Entity entity, String eepKey, String context, SimpleNetworkWrapper network)
    {
        sendToPlayersWatchingEntity(entity, PacketExtendedEntityPropertiesData.packetEntityData(entity, eepKey, context), network);
    }

    public static <UEntity extends Entity & PartialUpdateHandler> void sendEntityUpdatePacket(UEntity entity, String context, SimpleNetworkWrapper network)
    {
        sendToPlayersWatchingEntity(entity, PacketEntityData.packetEntityData(entity, context), network);
    }

    public static void sendToPlayersWatchingEntity(Entity entity, IMessage message, SimpleNetworkWrapper network)
    {
        if (!(entity.worldObj instanceof WorldServer))
            throw new UnsupportedOperationException();

        network.sendToDimension(message, entity.worldObj.provider.dimensionId);
//        ((WorldServer) entity.worldObj).getEntityTracker().func_151248_b(entity, PacketEntityData.packetEntityData(entity, context));
    }

    public static <UTileEntity extends TileEntity & PartialUpdateHandler> void sendTileEntityUpdatePacket(UTileEntity tileEntity, String context, SimpleNetworkWrapper network, EntityPlayer player)
    {
        if (!(player instanceof EntityPlayerMP))
            throw new UnsupportedOperationException();

        network.sendTo(PacketTileEntityData.packetEntityData(tileEntity, context), (EntityPlayerMP) player);
    }

    public static <UTileEntity extends TileEntity & PartialUpdateHandler> void sendTileEntityUpdatePacket(UTileEntity tileEntity, String context, SimpleNetworkWrapper network)
    {
        sendToPlayersWatchingChunk(tileEntity.getWorldObj(), tileEntity.xCoord / 16, tileEntity.zCoord / 16, network, PacketTileEntityData.packetEntityData(tileEntity, context));
    }

    public static void sendToPlayersWatchingChunk(World world, int chunkX, int chunkZ, SimpleNetworkWrapper network, IMessage message)
    {
        List<EntityPlayerMP> playersWatching = getPlayersWatchingChunk(world, chunkX, chunkZ);

        for (EntityPlayerMP playerMP : playersWatching)
        {
            network.sendTo(message, playerMP);
        }
    }

    public static void sendToPlayersWatchingChunk(World world, int chunkX, int chunkZ, Channel channel, Object message)
    {
        List<EntityPlayerMP> playersWatching = getPlayersWatchingChunk(world, chunkX, chunkZ);

        for (EntityPlayerMP playerMP : playersWatching)
        {
            sendToPlayer(channel, playerMP, message);
        }
    }

    public static void sendToPlayersWatchingChunk(World world, int chunkX, int chunkZ, Packet packet)
    {
        List<EntityPlayerMP> playersWatching = getPlayersWatchingChunk(world, chunkX, chunkZ);

        for (EntityPlayerMP playerMP : playersWatching)
        {
            playerMP.playerNetServerHandler.sendPacket(packet);
        }
    }

    public static void sendToPlayer(Channel channel, EntityPlayerMP playerMP, Object message)
    {
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(playerMP);
        channel.writeAndFlush(message);
    }

    public static List<EntityPlayerMP> getPlayersWatchingChunk(World world, int chunkX, int chunkZ)
    {
        if (world.isRemote || !(world instanceof WorldServer))
        {
            return Collections.emptyList();
        }

        ArrayList<EntityPlayerMP> playersWatching = new ArrayList<>();

        WorldServer server = (WorldServer) world;
        PlayerManager playerManager = server.getPlayerManager();

        List<EntityPlayerMP> players = server.playerEntities;
        for (EntityPlayerMP player : players)
        {
            if (playerManager.isPlayerWatchingChunk(player, chunkX, chunkZ))
            {
                playersWatching.add(player);
            }
        }

        return playersWatching;
    }
}
