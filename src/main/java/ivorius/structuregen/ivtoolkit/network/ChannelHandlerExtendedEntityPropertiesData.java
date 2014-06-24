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

package ivorius.structuregen.ivtoolkit.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IExtendedEntityProperties;

/**
 * Created by lukas on 13.04.14.
 */
@ChannelHandler.Sharable
public class ChannelHandlerExtendedEntityPropertiesData extends SimpleChannelInboundHandler<FMLProxyPacket>
{
    public final String packetChannel;

    public ChannelHandlerExtendedEntityPropertiesData(String packetChannel)
    {
        this.packetChannel = packetChannel;
    }

    /**
     * Sends an update packet for the entity properties to every watching player's client, if the current active side is the server.
     *
     * @param entity        The owning entity.
     * @param eepIdentifier The identifier for the extended entity properties. Must implement IExtendedEntityPropertiesUpdateData.
     * @param context       The update context for the packet. Will be passed in writeUpdateData and readUpdateData. Keep in mind you can also add your own additional context information to the buffer.
     */
    public void sendUpdatePacketSafe(Entity entity, String eepIdentifier, String context)
    {
        if (!entity.worldObj.isRemote && entity.worldObj instanceof WorldServer)
        {
            sendUpdatePacket(entity, eepIdentifier, context);
        }
    }

    /**
     * Sends an update packet for the entity properties to every watching player's client.
     * Do not invoke this method directly, always use {@link #sendUpdatePacketSafe(net.minecraft.entity.Entity, String, String)} instead.
     *
     * @param entity        The owning entity.
     * @param eepIdentifier The identifier for the extended entity properties. Must implement IExtendedEntityPropertiesUpdateData.
     * @param context       The update context for the packet. Will be passed in writeUpdateData and readUpdateData. Keep in mind you can also add your own additional context information to the buffer.
     */
    public void sendUpdatePacket(Entity entity, String eepIdentifier, String context)
    {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(entity.getEntityId());
        ByteBufUtils.writeUTF8String(buffer, eepIdentifier);
        ByteBufUtils.writeUTF8String(buffer, context);

        IExtendedEntityProperties extendedEntityProperties = entity.getExtendedProperties(eepIdentifier);
        if (!(extendedEntityProperties instanceof IExtendedEntityPropertiesUpdateData))
        {
            throw new IllegalArgumentException("IExtendedEntityProperties must implement IExtendedEntityPropertiesUpdateData to send update packets!");
        }

        ((IExtendedEntityPropertiesUpdateData) extendedEntityProperties).writeUpdateData(buffer, context);

        FMLProxyPacket packet = new FMLProxyPacket(buffer, packetChannel);
        ((WorldServer) entity.worldObj).getEntityTracker().func_151248_b(entity, packet);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FMLProxyPacket msg) throws Exception
    {
        ByteBuf buffer = msg.payload();
        int entityID = buffer.readInt();
        String eepIdentifier = ByteBufUtils.readUTF8String(buffer);
        String context = ByteBufUtils.readUTF8String(buffer);

        Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(entityID);

        if (entity != null)
        {
            IExtendedEntityProperties extendedEntityProperties = entity.getExtendedProperties(eepIdentifier);

            if (extendedEntityProperties instanceof IExtendedEntityPropertiesUpdateData)
            {
                ((IExtendedEntityPropertiesUpdateData) extendedEntityProperties).readUpdateData(buffer, context);
            }
        }
    }
}
