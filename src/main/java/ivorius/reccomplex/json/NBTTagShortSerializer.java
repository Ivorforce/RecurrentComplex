/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.json;

import com.google.gson.*;
import net.minecraft.nbt.NBTTagShort;

import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class NBTTagShortSerializer implements JsonSerializer<NBTTagShort>, JsonDeserializer<NBTTagShort>
{
    @Override
    public JsonElement serialize(NBTTagShort src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.getShort());
    }

    @Override
    public NBTTagShort deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        return new NBTTagShort(json.getAsShort());
    }
}
