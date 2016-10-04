/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import ivorius.reccomplex.item.ItemEventHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

/**
 * Created by lukas on 17.01.15.
 */
public class PacketItemEventHandler extends PacketEditInventoryItemHandler<PacketItemEvent>
{
    @Override
    public void affectItem(EntityPlayerMP player, ItemStack stack, PacketItemEvent message)
    {
        if (stack != null)
        {
            ItemEventHandler itemEventHandler = (ItemEventHandler) stack.getItem();
            itemEventHandler.onClientEvent(message.context, message.payload, player, stack, message.getInventorySlot());
        }
    }
}
