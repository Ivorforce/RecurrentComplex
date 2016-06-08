/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.json;

import com.google.gson.*;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;

import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class NBTTagListSerializer implements JsonSerializer<NBTTagList>, JsonDeserializer<NBTTagList>
{
    @Override
    public JsonElement serialize(NBTTagList src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonArray array = new JsonArray();
        NBTTagList copy = (NBTTagList) src.copy();

        while (copy.tagCount() > 0)
        {
            array.add(context.serialize(copy.removeTag(0)));
        }

        return array;
    }

    @Override
    public NBTTagList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if (json.isJsonArray())
        {
            JsonArray jsonArray = json.getAsJsonArray();
            NBTTagList list = new NBTTagList();

            for (JsonElement element : jsonArray)
            {
                list.appendTag(context.deserialize(element, NbtToJson.getNBTTypeSmart(element)));
            }

            return list;
        }

        return null;
    }
}
