/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import ivorius.reccomplex.worldgen.inventory.GenericInventoryGenerator;
import ivorius.reccomplex.worldgen.inventory.InventoryGenerationHandler;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditInventoryGenerator implements IMessage
{
    private String key;
    private GenericInventoryGenerator inventoryGenerator;

    public PacketEditInventoryGenerator()
    {
    }

    public PacketEditInventoryGenerator(String key, GenericInventoryGenerator inventoryGenerator)
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

    public GenericInventoryGenerator getInventoryGenerator()
    {
        return inventoryGenerator;
    }

    public void setInventoryGenerator(GenericInventoryGenerator inventoryGenerator)
    {
        this.inventoryGenerator = inventoryGenerator;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        key = ByteBufUtils.readUTF8String(buf);
        String json = ByteBufUtils.readUTF8String(buf);
        inventoryGenerator = InventoryGenerationHandler.createInventoryGeneratorFromJSON(json);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, key);
        String json = InventoryGenerationHandler.createJSONFromInventoryGenerator(inventoryGenerator);
        ByteBufUtils.writeUTF8String(buf, json);
    }
}
