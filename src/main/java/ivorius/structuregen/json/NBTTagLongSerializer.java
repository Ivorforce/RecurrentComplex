/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.json;

import com.google.gson.*;
import net.minecraft.nbt.NBTTagLong;

import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class NBTTagLongSerializer implements JsonSerializer<NBTTagLong>, JsonDeserializer<NBTTagLong>
{
    @Override
    public JsonElement serialize(NBTTagLong src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.func_150291_c());
    }

    @Override
    public NBTTagLong deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        return new NBTTagLong(json.getAsLong());
    }
}
