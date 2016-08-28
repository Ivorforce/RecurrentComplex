/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import io.netty.buffer.ByteBuf;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollectionRegistry;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditInvGen implements IMessage
{
    private String key;
    private Component inventoryGenerator;

    public PacketEditInvGen()
    {
    }

    public PacketEditInvGen(String key, Component inventoryGenerator)
    {
        this.key = key;
        this.inventoryGenerator = inventoryGenerator;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public Component getInventoryGenerator()
    {
        return inventoryGenerator;
    }

    public void setInventoryGenerator(Component inventoryGenerator)
    {
        this.inventoryGenerator = inventoryGenerator;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        key = ByteBufUtils.readUTF8String(buf);
        inventoryGenerator = GenericItemCollectionRegistry.INSTANCE.readComponent(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, key);
        GenericItemCollectionRegistry.INSTANCE.writeComponent(buf, inventoryGenerator);
    }
}
