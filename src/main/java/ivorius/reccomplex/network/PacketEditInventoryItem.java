/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * Created by lukas on 17.01.15.
 */
public class PacketEditInventoryItem implements IMessage
{
    private int inventorySlot;

    public PacketEditInventoryItem()
    {
    }

    public PacketEditInventoryItem(int inventorySlot)
    {
        this.inventorySlot = inventorySlot;
    }

    public int getInventorySlot()
    {
        return inventorySlot;
    }

    public void setInventorySlot(int inventorySlot)
    {
        this.inventorySlot = inventorySlot;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        inventorySlot = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(inventorySlot);
    }
}
