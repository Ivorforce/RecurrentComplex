/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import ivorius.reccomplex.items.RCItems;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

/**
 * Created by lukas on 17.01.15.
 */
public class PacketEditInvGenMultiTagHandler extends PacketEditInventoryItemHandler<PacketEditInvGenMultiTag>
{
    @Override
    public void affectItem(EntityPlayerMP player, ItemStack stack, PacketEditInvGenMultiTag message)
    {
        if (stack != null)
        {
            RCItems.inventoryGenerationTag.setGenerationCount(stack, message.itemCount);
        }
    }
}
