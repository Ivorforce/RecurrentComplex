/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.json;

import com.google.gson.*;
import net.minecraft.nbt.NBTTagFloat;

import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class NBTTagFloatSerializer implements JsonSerializer<NBTTagFloat>, JsonDeserializer<NBTTagFloat>
{
    @Override
    public JsonElement serialize(NBTTagFloat src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.func_150288_h());
    }

    @Override
    public NBTTagFloat deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        return new NBTTagFloat(json.getAsFloat());
    }
}
