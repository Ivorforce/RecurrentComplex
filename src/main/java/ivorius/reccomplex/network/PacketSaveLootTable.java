/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
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
public class PacketSaveLootTable implements IMessage
{
    private String key;
    private Component component;

    private SaveDirectoryData.Result saveDirectoryDataResult;

    public PacketSaveLootTable()
    {
    }

    public PacketSaveLootTable(String key, Component component, SaveDirectoryData.Result saveDirectoryDataResult)
    {
        this.key = key;
        this.component = component;
        this.saveDirectoryDataResult = saveDirectoryDataResult;
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

    public SaveDirectoryData.Result getSaveDirectoryDataResult()
    {
        return saveDirectoryDataResult;
    }

    public void setSaveDirectoryDataResult(SaveDirectoryData.Result saveDirectoryDataResult)
    {
        this.saveDirectoryDataResult = saveDirectoryDataResult;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        key = ByteBufUtils.readUTF8String(buf);
        component = ItemCollectionSaveHandler.INSTANCE.read(buf);
        saveDirectoryDataResult = SaveDirectoryData.Result.readFrom(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, key);
        ItemCollectionSaveHandler.INSTANCE.write(buf, component);
        saveDirectoryDataResult.writeTo(buf);
    }
}
