/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import ivorius.ivtoolkit.network.PacketExtendedEntityPropertiesData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

/**
 * Created by lukas on 27.09.14.
 */
public class RCNetworkHelperServer
{
    public static void sendEEPUpdatePacketToPlayer(Entity entity, String eepKey, String context, SimpleNetworkWrapper network, EntityPlayer player, Object... params)
    {
        if (!(player instanceof EntityPlayerMP))
            throw new UnsupportedOperationException();

        network.sendTo(PacketExtendedEntityPropertiesData.packetEntityData(entity, eepKey, context, params), (EntityPlayerMP) player);
    }

    public static void sendEEPUpdatePacket(Entity entity, String eepKey, String context, SimpleNetworkWrapper network, Object... params)
    {
        if (entity.worldObj.isRemote)
            throw new UnsupportedOperationException();

        for (EntityPlayer player : ((WorldServer) entity.worldObj).getEntityTracker().getTrackingPlayers(entity))
            sendEEPUpdatePacketToPlayer(entity, eepKey, context, network, player, params);

        if (entity instanceof EntityPlayer) // Players don't 'track' themselves
            sendEEPUpdatePacketToPlayer(entity, eepKey, context, network, (EntityPlayer) entity, params);
    }
}