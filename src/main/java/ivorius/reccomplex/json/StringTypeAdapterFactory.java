/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.json;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.*;
import org.apache.commons.lang3.tuple.Pair;


import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lukas on 25.05.14.
 */
public class StringTypeAdapterFactory<B> implements JsonSerializer<B>, JsonDeserializer<B>
{
    private Map<String, JsonDeserializer<? extends B>> deserializerMap = new HashMap<>();
    private Map<Class<? extends B>, JsonSerializer<? extends B>> serializerMap = new HashMap<>();
    private BiMap<Class<? extends B>, String> classMap = HashBiMap.create();

    private Map<String, Pair<Class<? extends B>, JsonDeserializer<? extends B>>> legacyMap = new HashMap<>();

    private Gson gson = new Gson();

    private String typeKey;
    private String objectKey;

    public StringTypeAdapterFactory(String objectKey, String typeKey)
    {
        if (objectKey.equals(typeKey))
            throw new IllegalArgumentException("Type key must be different from object key");

        this.objectKey = objectKey;
        this.typeKey = typeKey;
    }

    public StringTypeAdapterFactory()
    {
        this("object", "type");
    }

    public String getTypeKey()
    {
        return typeKey;
    }

    public String getObjectKey()
    {
        return objectKey;
    }

    public <T extends B> void registerLegacy(String id, Class<? extends T> clazz, @Nullable JsonDeserializer<T> deserializer)
    {
        legacyMap.put(id, Pair.of(clazz, deserializer));
    }

    public <T extends B> void register(String id, Class<? extends T> clazz, @Nullable JsonSerializer<T> serializer, @Nullable JsonDeserializer<T> deserializer)
    {
        deserializerMap.put(id, deserializer);
        serializerMap.put(clazz, serializer);
        classMap.put(clazz, id);
    }

    public <K extends B, MultiSerializer extends JsonSerializer<K> & JsonDeserializer<K>> void register(String id, Class<K> clazz, @Nullable MultiSerializer multiSerializer)
    {
        register(id, clazz, multiSerializer, multiSerializer);
    }

    @Nullable
    public JsonDeserializer<? extends B> deserializer(String key)
    {
        return deserializerMap.get(key);
    }

    @Nullable
    public <T extends B> JsonSerializer<T> serializer(Class<? extends T> aClass)
    {
        //noinspection unchecked
        return (JsonSerializer<T>) serializerMap.get(aClass);
    }

    public String type(Class<? extends B> aClass)
    {
        return classMap.get(aClass);
    }

    public Class<? extends B> objectClass(String type)
    {
        return classMap.inverse().get(type);
    }

    public Collection<String> allIDs()
    {
        return deserializerMap.keySet();
    }

    @Override
    public B deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if (json.isJsonObject())
        {
            JsonObject jsonObject = json.getAsJsonObject();
            if (jsonObject.has(typeKey) && jsonObject.has(objectKey))
            {
                String type = JsonUtils.getString(jsonObject, typeKey);
                JsonElement objectElement = jsonObject.get(objectKey);

                Pair<Class<? extends B>, JsonDeserializer<? extends B>> legacy = legacyMap.get(type);
                if (legacy != null) {
                    return legacy.getRight().deserialize(objectElement, legacy.getLeft(), context);
                }

                Class<? extends B> typeClass = objectClass(type);

                if (typeClass == null) {
                    throw new JsonParseException("Unknown type: " + type);
                }

                JsonDeserializer<? extends B> deserializer = deserializer(type);

                return deserializer != null
                    ? deserializer.deserialize(objectElement, typeClass, context)
                    : gson.fromJson(objectElement, typeClass);
            }
        }

        return null;
    }

    @Override
    public JsonElement serialize(B src, Type typeOfSrc, JsonSerializationContext context)
    {
        @SuppressWarnings("unchecked") Class<? extends B> objectClass = (Class<? extends B>) src.getClass();

        String id = type(objectClass);
        JsonSerializer serializer = serializer(objectClass);

        if (id != null)
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(typeKey, id);
            //noinspection unchecked
            jsonObject.add(objectKey, serializer != null
                    ? serializer.serialize(src, typeOfSrc, context)
                    : gson.toJsonTree(src, typeOfSrc)
            );
            return jsonObject;
        }

        return null;
    }
}
