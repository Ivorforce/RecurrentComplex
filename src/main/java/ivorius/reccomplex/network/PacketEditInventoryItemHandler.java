/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import ivorius.ivtoolkit.network.SchedulingMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Created by lukas on 17.01.15.
 */
public abstract class PacketEditInventoryItemHandler<P extends PacketEditInventoryItem> extends SchedulingMessageHandler<P, IMessage>
{
    @Override
    public void processServer(P message, MessageContext ctx, WorldServer server)
    {
        NetHandlerPlayServer playServer = ctx.getServerHandler();
        EntityPlayerMP player = playServer.player;
        affectItem(player, player.inventory.getStackInSlot(message.getInventorySlot()), message);
        player.openContainer.detectAndSendChanges();
    }

    public abstract void affectItem(EntityPlayerMP player, ItemStack stack, P message);
}
