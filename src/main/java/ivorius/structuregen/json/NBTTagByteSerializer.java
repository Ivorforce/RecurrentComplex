package ivorius.structuregen.json;

import com.google.gson.*;
import net.minecraft.nbt.NBTTagByte;

import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class NBTTagByteSerializer implements JsonSerializer<NBTTagByte>, JsonDeserializer<NBTTagByte>
{
    @Override
    public JsonElement serialize(NBTTagByte src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.func_150290_f());
    }

    @Override
    public NBTTagByte deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        return new NBTTagByte(json.getAsByte());
    }
}
