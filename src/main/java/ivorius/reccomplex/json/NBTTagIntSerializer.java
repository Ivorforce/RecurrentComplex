/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.json;

import com.google.gson.*;
import net.minecraft.nbt.NBTTagInt;

import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class NBTTagIntSerializer implements JsonSerializer<NBTTagInt>, JsonDeserializer<NBTTagInt>
{
    @Override
    public JsonElement serialize(NBTTagInt src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.getInt());
    }

    @Override
    public NBTTagInt deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        return new NBTTagInt(json.getAsInt());
    }
}
