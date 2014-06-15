package ivorius.structuregen.json;

import com.google.gson.*;
import net.minecraft.nbt.NBTTagEnd;

import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class NBTTagEndSerializer implements JsonSerializer<NBTTagEnd>, JsonDeserializer<NBTTagEnd>
{
    @Override
    public JsonElement serialize(NBTTagEnd src, Type typeOfSrc, JsonSerializationContext context)
    {
        return JsonNull.INSTANCE;
    }

    @Override
    public NBTTagEnd deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        return new NBTTagEnd();
    }
}
