/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import ivorius.ivtoolkit.tools.IvSideClient;
import ivorius.reccomplex.gui.container.IvGuiRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketOpenGuiHandler extends SchedulingMessageHandler<PacketOpenGui, IMessage>
{
    @Override
    @SideOnly(Side.CLIENT)
    public void processClient(PacketOpenGui message, MessageContext ctx)
    {
        IvGuiRegistry.INSTANCE.openGuiJustClient(IvSideClient.getClientPlayer(), message.modId, message.modGuiId, message.data);
    }

    @Override
    public void processServer(PacketOpenGui message, MessageContext ctx, WorldServer world)
    {
        EntityPlayerMP playerEntity = ctx.getServerHandler().player;
        IvGuiRegistry.INSTANCE.openGui(playerEntity, message.modId, message.modGuiId, message.data);
    }
}

