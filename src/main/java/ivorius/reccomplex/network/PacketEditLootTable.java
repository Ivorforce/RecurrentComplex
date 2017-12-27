/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.network;

import io.netty.buffer.ByteBuf;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.world.storage.loot.GenericLootTable.Component;
import ivorius.reccomplex.world.storage.loot.ItemCollectionSaveHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketEditLootTable implements IMessage
{
    private String key;
    private Component component;

    private SaveDirectoryData saveDirectoryData;

    public PacketEditLootTable()
    {
    }

    public PacketEditLootTable(String key, Component component, SaveDirectoryData saveDirectoryData)
    {
        this.key = key;
        this.component = component;
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

    public Component getComponent()
    {
        return component;
    }

    public void setComponent(Component component)
    {
        this.component = component;
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
        component = ItemCollectionSaveHandler.INSTANCE.read(buf);
        saveDirectoryData = SaveDirectoryData.readFrom(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, key);
        ItemCollectionSaveHandler.INSTANCE.write(buf, component);
        saveDirectoryData.writeTo(buf);
    }
}
