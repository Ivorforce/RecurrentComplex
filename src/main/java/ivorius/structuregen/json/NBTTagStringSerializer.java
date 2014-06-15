/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.json;

import com.google.gson.*;
import net.minecraft.nbt.NBTTagString;

import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class NBTTagStringSerializer implements JsonSerializer<NBTTagString>, JsonDeserializer<NBTTagString>
{
    @Override
    public JsonElement serialize(NBTTagString src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.func_150285_a_());
    }

    @Override
    public NBTTagString deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        return new NBTTagString(json.getAsString());
    }
}
