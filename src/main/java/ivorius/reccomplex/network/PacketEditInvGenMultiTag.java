/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import io.netty.buffer.ByteBuf;
import ivorius.ivtoolkit.gui.IntegerRange;
import net.minecraft.item.ItemStack;

/**
 * Created by lukas on 17.01.15.
 */
public class PacketEditInvGenMultiTag extends PacketEditInventoryItem
{
    public IntegerRange itemCount;

    public PacketEditInvGenMultiTag()
    {
    }

    public PacketEditInvGenMultiTag(int inventorySlot, IntegerRange itemCount)
    {
        super(inventorySlot);
        this.itemCount = itemCount;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        super.fromBytes(buf);
        itemCount = new IntegerRange(buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(itemCount.getMin()).writeInt(itemCount.getMax());
    }
}
