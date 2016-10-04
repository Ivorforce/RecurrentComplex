/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.item;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Created by lukas on 11.02.15.
 */
public interface ItemEventHandler
{
    void onClientEvent(String context, ByteBuf payload, EntityPlayer sender, ItemStack stack, int itemSlot);
}
