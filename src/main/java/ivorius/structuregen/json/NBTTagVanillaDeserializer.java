/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;

import java.lang.reflect.Type;

/**
 * Created by lukas on 25.05.14.
 */
public class NBTTagVanillaDeserializer implements JsonDeserializer<NBTBase>
{
    @Override
    public NBTBase deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        try
        {
            NBTBase nbtBase = JsonToNBT.func_150315_a(json.toString());
            return nbtBase;
        }
        catch (NBTException e)
        {
            e.printStackTrace();
        }

        throw new JsonParseException("Could not parse JSON to NBT");
    }
}
