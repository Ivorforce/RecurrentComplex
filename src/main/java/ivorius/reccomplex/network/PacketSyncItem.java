/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import ivorius.reccomplex.items.ItemSyncable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by lukas on 17.01.15.
 */
public class PacketSyncItem extends PacketEditInventoryItem
{
    public NBTTagCompound data;

    public PacketSyncItem()
    {
    }

    public PacketSyncItem(int inventorySlot, ItemStack stack)
    {
        super(inventorySlot);
        ItemSyncable itemSyncable = (ItemSyncable) stack.getItem();
        data = new NBTTagCompound();
        itemSyncable.writeSyncedNBT(data, stack);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        super.fromBytes(buf);
        data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        super.toBytes(buf);
        ByteBufUtils.writeTag(buf, data);
    }
}
