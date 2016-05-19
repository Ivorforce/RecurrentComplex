/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.json;

import com.google.gson.*;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by lukas on 25.05.14.
 */
public class NBTTagCompoundSerializer implements JsonSerializer<NBTTagCompound>, JsonDeserializer<NBTTagCompound>
{
    @Override
    public JsonElement serialize(NBTTagCompound src, Type typeOfSrc, JsonSerializationContext context)
    {
        JsonObject jsonObject = new JsonObject();

        for (Object key : src.getKeySet())
        {
            String keyString = (String) key;
            jsonObject.add(keyString, context.serialize(src.getTag(keyString)));
        }

        return jsonObject;
    }


    @Override
    public NBTTagCompound deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if (json.isJsonObject())
        {
            JsonObject jsonObject = json.getAsJsonObject();
            NBTTagCompound compound = new NBTTagCompound();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
            {
                compound.setTag(entry.getKey(), context.<NBTBase>deserialize(entry.getValue(), NbtToJson.getNBTTypeSmart(entry.getValue())));
            }

            return compound;
        }

        return null;
    }
}
