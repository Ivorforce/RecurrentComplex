/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;

/**
 * Created by lukas on 17.01.15.
 */
public abstract class PacketEditInventoryItemHandler<P extends PacketEditInventoryItem> implements IMessageHandler<P, IMessage>
{
    @Override
    public IMessage onMessage(P message, MessageContext ctx)
    {
        if (ctx.side == Side.SERVER)
        {
            NetHandlerPlayServer playServer = ctx.getServerHandler();
            EntityPlayerMP player = playServer.playerEntity;
            affectItem(player, player.inventory.getStackInSlot(message.getInventorySlot()), message);
            player.openContainer.detectAndSendChanges();
        }

        return null;
    }

    public abstract void affectItem(EntityPlayerMP player, ItemStack stack, P message);
}
