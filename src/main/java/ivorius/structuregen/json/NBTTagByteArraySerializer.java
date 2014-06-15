package ivorius.structuregen.json;

import com.google.gson.*;
import net.minecraft.nbt.NBTTagByteArray;

import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class NBTTagByteArraySerializer implements JsonSerializer<NBTTagByteArray>, JsonDeserializer<NBTTagByteArray>
{
    @Override
    public JsonElement serialize(NBTTagByteArray src, Type typeOfSrc, JsonSerializationContext context)
    {
        return context.serialize(src.func_150292_c(), byte[].class);
    }

    @Override
    public NBTTagByteArray deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        return new NBTTagByteArray(context.<byte[]>deserialize(json, byte[].class));
    }
}
