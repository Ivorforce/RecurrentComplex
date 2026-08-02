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
    // Compact (no pretty-printing) variant, ~2-3x smaller output, used only for network payloads
    // as a stopgap for #314 (see write()). Files keep using the pretty-printed `gson`.
    private Gson gsonCompact = createGson(false);

    public Gson createGson()
    {
        return createGson(true);
    }

    public Gson createGson(boolean prettyPrinting)
    {
        GsonBuilder builder = new GsonBuilder();

        if (prettyPrinting)
            builder.setPrettyPrinting();
        builder.registerTypeAdapter(GenericLootTable.Component.class, new GenericLootTable.Component.Serializer());
        NBTToJson.registerSafeNBTSerializer(builder);

        return builder.create();
    }

    // Uses compact JSON to keep the payload well below the 32767-byte writeUTF8String ceiling, which
    // otherwise crashes on large loot tables with "string too long for this encoding" (#314). This
    // only raises the effective size limit; it does not remove it.
    // TODO (next minor bump): switch this and read() to an int-length-prefixed UTF-8 encoding to drop
    // the ceiling entirely. Deferred because it changes the packet wire format (PacketEditLootTable /
    // PacketSaveLootTable / RCGuiHandler sync) and would break server<->client compat across patch
    // versions. The on-disk format (toJSON/fromJSON) is unaffected either way.
    public void write(ByteBuf data, GenericLootTable.Component component)
    {
        ByteBufUtils.writeUTF8String(data, gsonCompact.toJson(component, GenericLootTable.Component.class));
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
