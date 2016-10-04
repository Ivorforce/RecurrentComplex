/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.network;

import io.netty.buffer.ByteBuf;
import ivorius.reccomplex.utils.SaveDirectoryData;
import ivorius.reccomplex.world.storage.loot.GenericItemCollection.Component;
import ivorius.reccomplex.world.storage.loot.ItemCollectionSaveHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Created by lukas on 03.08.14.
 */
public class PacketSaveInvGenComponent implements IMessage
{
    private String key;
    private Component inventoryGenerator;

    private SaveDirectoryData.Result saveDirectoryDataResult;

    public PacketSaveInvGenComponent()
    {
    }

    public PacketSaveInvGenComponent(String key, Component inventoryGenerator, SaveDirectoryData.Result saveDirectoryDataResult)
    {
        this.key = key;
        this.inventoryGenerator = inventoryGenerator;
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

    public Component getInventoryGenerator()
    {
        return inventoryGenerator;
    }

    public void setInventoryGenerator(Component inventoryGenerator)
    {
        this.inventoryGenerator = inventoryGenerator;
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
        inventoryGenerator = ItemCollectionSaveHandler.INSTANCE.read(buf);
        saveDirectoryDataResult = SaveDirectoryData.Result.readFrom(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, key);
        ItemCollectionSaveHandler.INSTANCE.write(buf, inventoryGenerator);
        saveDirectoryDataResult.writeTo(buf);
    }
}
