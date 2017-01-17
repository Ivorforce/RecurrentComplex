/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.storage.loot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import ivorius.reccomplex.json.NBTToJson;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nullable;

/**
 * Created by lukas on 25.05.14.
 */
public class ItemCollectionSaveHandler
{
    public static final ItemCollectionSaveHandler INSTANCE = new ItemCollectionSaveHandler();

    private Gson gson = createGson();

    public Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();

        builder.setPrettyPrinting();
        builder.registerTypeAdapter(GenericItemCollection.Component.class, new GenericItemCollection.Component.Serializer());
        NBTToJson.registerSafeNBTSerializer(builder);

        return builder.create();
    }

    public void write(ByteBuf data, GenericItemCollection.Component component)
    {
        ByteBufUtils.writeUTF8String(data, toJSON(component));
    }

    @Nullable
    public GenericItemCollection.Component read(ByteBuf data)
    {
        try
        {
            return this.fromJSON(ByteBufUtils.readUTF8String(data));
        }
        catch (InventoryLoadException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public String toJSON(GenericItemCollection.Component inventoryGenerator)
    {
        return gson.toJson(inventoryGenerator, GenericItemCollection.Component.class);
    }

    public GenericItemCollection.Component fromJSON(String json) throws InventoryLoadException
    {
        try
        {
            return gson.fromJson(json, GenericItemCollection.Component.class);
        }
        catch (JsonSyntaxException e)
        {
            throw new InventoryLoadException(e);
        }
    }
}
