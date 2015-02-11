/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import ivorius.reccomplex.items.ItemSyncable;
import ivorius.reccomplex.items.RCItems;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

/**
 * Created by lukas on 17.01.15.
 */
public class PacketSyncItemHandler extends PacketEditInventoryItemHandler<PacketSyncItem>
{
    @Override
    public void affectItem(EntityPlayerMP player, ItemStack stack, PacketSyncItem message)
    {
        if (stack != null)
        {
            ItemSyncable itemSyncable = (ItemSyncable) stack.getItem();
            itemSyncable.readSyncedNBT(message.data, stack);
        }
    }
}
