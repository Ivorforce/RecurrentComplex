/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.inventory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import ivorius.reccomplex.files.RCFileSuffix;
import ivorius.reccomplex.files.RCFileTypeRegistry;
import ivorius.reccomplex.json.NbtToJson;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollection.Component;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

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
        builder.registerTypeAdapter(Component.class, new Component.Serializer());
        NbtToJson.registerSafeNBTSerializer(builder);

        return builder.create();
    }

    public void write(ByteBuf data, Component component)
    {
        ByteBufUtils.writeUTF8String(data, toJSON(component));
    }

    @Nullable
    public Component read(ByteBuf data)
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

    public String toJSON(Component inventoryGenerator)
    {
        return gson.toJson(inventoryGenerator, Component.class);
    }

    public Component fromJSON(String json) throws InventoryLoadException
    {
        try
        {
            return gson.fromJson(json, Component.class);
        }
        catch (JsonSyntaxException e)
        {
            throw new InventoryLoadException(e);
        }
    }
}
