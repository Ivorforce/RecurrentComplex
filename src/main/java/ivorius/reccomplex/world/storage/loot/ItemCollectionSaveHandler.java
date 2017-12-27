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
        builder.registerTypeAdapter(GenericLootTable.Component.class, new GenericLootTable.Component.Serializer());
        NBTToJson.registerSafeNBTSerializer(builder);

        return builder.create();
    }

    public void write(ByteBuf data, GenericLootTable.Component component)
    {
        ByteBufUtils.writeUTF8String(data, toJSON(component));
    }

    @Nullable
    public GenericLootTable.Component read(ByteBuf data)
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

    public String toJSON(GenericLootTable.Component component)
    {
        return gson.toJson(component, GenericLootTable.Component.class);
    }

    public GenericLootTable.Component fromJSON(String json) throws InventoryLoadException
    {
        try
        {
            return gson.fromJson(json, GenericLootTable.Component.class);
        }
        catch (JsonSyntaxException e)
        {
            throw new InventoryLoadException(e);
        }
    }
}
