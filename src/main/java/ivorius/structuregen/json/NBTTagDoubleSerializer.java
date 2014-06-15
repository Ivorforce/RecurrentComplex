package ivorius.structuregen.json;

import com.google.gson.*;
import net.minecraft.nbt.NBTTagDouble;

import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class NBTTagDoubleSerializer implements JsonSerializer<NBTTagDouble>, JsonDeserializer<NBTTagDouble>
{
    @Override
    public JsonElement serialize(NBTTagDouble src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.func_150286_g());
    }

    @Override
    public NBTTagDouble deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        return new NBTTagDouble(json.getAsDouble());
    }
}
