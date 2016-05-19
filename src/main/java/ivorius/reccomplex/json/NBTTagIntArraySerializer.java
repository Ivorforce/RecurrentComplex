/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.json;

import com.google.gson.*;
import net.minecraft.nbt.NBTTagIntArray;

import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class NBTTagIntArraySerializer implements JsonSerializer<NBTTagIntArray>, JsonDeserializer<NBTTagIntArray>
{
    @Override
    public JsonElement serialize(NBTTagIntArray src, Type typeOfSrc, JsonSerializationContext context)
    {
        return context.serialize(src.getIntArray(), int[].class);
    }

    @Override
    public NBTTagIntArray deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        return new NBTTagIntArray(context.<int[]>deserialize(json, int[].class));
    }
}
