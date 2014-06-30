/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.json;

import com.google.gson.*;
import net.minecraft.nbt.*;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;

/**
 * Created by lukas on 30.05.14.
 */
public class NbtToJson
{
    public static final Gson nbtJson = createGson();

    public static Gson createGson()
    {
        GsonBuilder builder = new GsonBuilder();
        registerAllNBTSerializers(builder);
        return builder.create();
    }

    public static StringTypeAdapterFactory<NBTBase> createSafeNBTSerializer()
    {
        StringTypeAdapterFactory<NBTBase> nbtAdapterFactory = new StringTypeAdapterFactory<>("nbt", "nbtType");
        nbtAdapterFactory.register("byteArray", NBTTagByteArray.class, new NBTTagByteArraySerializer());
        nbtAdapterFactory.register("byte", NBTTagByte.class, new NBTTagByteSerializer());
        nbtAdapterFactory.register("compound", NBTTagCompound.class, new NBTTagCompoundSerializer());
        nbtAdapterFactory.register("double", NBTTagDouble.class, new NBTTagDoubleSerializer());
        nbtAdapterFactory.register("end", NBTTagEnd.class, new NBTTagEndSerializer());
        nbtAdapterFactory.register("float", NBTTagFloat.class, new NBTTagFloatSerializer());
        nbtAdapterFactory.register("intArray", NBTTagIntArray.class, new NBTTagIntArraySerializer());
        nbtAdapterFactory.register("int", NBTTagInt.class, new NBTTagIntSerializer());
        nbtAdapterFactory.register("list", NBTTagList.class, new NBTTagListSerializer());
        nbtAdapterFactory.register("long", NBTTagLong.class, new NBTTagLongSerializer());
        nbtAdapterFactory.register("short", NBTTagShort.class, new NBTTagShortSerializer());
        nbtAdapterFactory.register("string", NBTTagString.class, new NBTTagStringSerializer());
        return nbtAdapterFactory;
    }

    public static void registerSafeNBTSerializer(GsonBuilder builder)
    {
        builder.registerTypeHierarchyAdapter(NBTBase.class, createSafeNBTSerializer());
    }

    public static void registerAllNBTSerializers(GsonBuilder builder)
    {
        builder.registerTypeAdapter(NBTTagByteArray.class, new NBTTagByteArraySerializer());
        builder.registerTypeAdapter(NBTTagByte.class, new NBTTagByteSerializer());
        builder.registerTypeAdapter(NBTTagCompound.class, new NBTTagCompoundSerializer());
        builder.registerTypeAdapter(NBTTagDouble.class, new NBTTagDoubleSerializer());
        builder.registerTypeAdapter(NBTTagEnd.class, new NBTTagEndSerializer());
        builder.registerTypeAdapter(NBTTagFloat.class, new NBTTagFloatSerializer());
        builder.registerTypeAdapter(NBTTagIntArray.class, new NBTTagIntArraySerializer());
        builder.registerTypeAdapter(NBTTagInt.class, new NBTTagIntSerializer());
        builder.registerTypeAdapter(NBTTagList.class, new NBTTagListSerializer());
        builder.registerTypeAdapter(NBTTagLong.class, new NBTTagLongSerializer());
        builder.registerTypeAdapter(NBTTagShort.class, new NBTTagShortSerializer());
        builder.registerTypeAdapter(NBTTagString.class, new NBTTagStringSerializer());
    }

    public static void registerSmartNBTSerializer(GsonBuilder builder)
    {
        builder.registerTypeHierarchyAdapter(NBTBase.class, new NBTBaseSerializerSmart());
    }

    public static Class<? extends NBTBase> getNBTTypeSmart(JsonElement element)
    {
        if (element.isJsonArray())
        {
            JsonArray array = element.getAsJsonArray();

            if (array.size() == 0)
            {
                return NBTTagList.class;
            }

            boolean allByte = true;
            boolean allInt = true;
            for (JsonElement arrayElement : array)
            {
                if (arrayElement.isJsonPrimitive())
                {
                    JsonPrimitive primitive = arrayElement.getAsJsonPrimitive();
                    if (!(primitive.isNumber() && primitive.getAsNumber() instanceof Byte))
                    {
                        allByte = false;
                    }
                    if (!(primitive.isNumber() && primitive.getAsNumber() instanceof Integer))
                    {
                        allInt = false;
                    }
                }
                else
                {
                    allByte = false;
                    allInt = false;
                }
            }

            if (allByte)
            {
                return NBTTagByteArray.class;
            }
            if (allInt)
            {
                return NBTTagIntArray.class;
            }

            return NBTTagList.class;
        }
        else if (element.isJsonObject())
        {
            return NBTTagCompound.class;
        }
        else if (element.isJsonPrimitive())
        {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString())
            {
                return NBTTagString.class;
            }
            else if (primitive.isNumber())
            {
                return NBTBase.NBTPrimitive.class;
            }
        }

        return null;
    }

    public static NBTTagCompound getNBTFromBase64(String elementString)
    {
        byte[] nbtBytes = DatatypeConverter.parseBase64Binary(elementString);

        try
        {
            return CompressedStreamTools.decompress(nbtBytes);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static String getBase64FromNBT(NBTTagCompound compound)
    {
        byte[] worldDataByteArray;

        try
        {
            worldDataByteArray = CompressedStreamTools.compress(compound);
            return DatatypeConverter.printBase64Binary(worldDataByteArray);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
