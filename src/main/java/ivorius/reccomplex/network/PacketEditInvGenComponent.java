/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import io.netty.buffer.ByteBuf;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;
import ivorius.reccomplex.worldgen.inventory.ItemCollectionSaveHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditInvGenComponent implements IMessage
{
    private String key;
    private Component inventoryGenerator;

    private SaveDirectoryData saveDirectoryData;

    public PacketEditInvGenComponent()
    {
    }

    public PacketEditInvGenComponent(String key, Component inventoryGenerator, SaveDirectoryData saveDirectoryData)
    {
        this.key = key;
        this.inventoryGenerator = inventoryGenerator;
        this.saveDirectoryData = saveDirectoryData;
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

    public SaveDirectoryData getSaveDirectoryData()
    {
        return saveDirectoryData;
    }

    public void setSaveDirectoryData(SaveDirectoryData saveDirectoryData)
    {
        this.saveDirectoryData = saveDirectoryData;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        key = ByteBufUtils.readUTF8String(buf);
        inventoryGenerator = ItemCollectionSaveHandler.INSTANCE.read(buf);
        saveDirectoryData = SaveDirectoryData.readFrom(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, key);
        ItemCollectionSaveHandler.INSTANCE.write(buf, inventoryGenerator);
        saveDirectoryData.writeTo(buf);
    }
}
