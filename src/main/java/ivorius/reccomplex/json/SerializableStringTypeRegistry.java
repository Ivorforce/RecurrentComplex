/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.json;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import java.util.Collection;

/**
 * Created by lukas on 19.02.15.
 */
public class SerializableStringTypeRegistry<T>
{
    private StringTypeAdapterFactory<T> adapterFactory;
    private Class<T> typeClass;

    public SerializableStringTypeRegistry(String objectKey, String typeKey, Class<T> typeClass)
    {
        adapterFactory = new StringTypeAdapterFactory<>(objectKey, typeKey);
        this.typeClass = typeClass;
    }

    public void constructGson(GsonBuilder builder)
    {
        builder.registerTypeHierarchyAdapter(typeClass, adapterFactory);
    }

    public StringTypeAdapterFactory<T> adapterFactory()
    {
        return adapterFactory;
    }

    public <TI extends T> void registerType(String id, Class<TI> clazz, JsonSerializer<TI> serializer, JsonDeserializer<TI> deserializer)
    {
        adapterFactory.register(id, clazz, serializer, deserializer);
    }

    public <TI extends T, S extends JsonSerializer<TI> & JsonDeserializer<TI>> void registerType(String id, Class<TI> clazz, S serializer)
    {
        registerType(id, clazz, serializer, serializer);
    }

    public String typeKey()
    {
        return adapterFactory.getTypeKey();
    }

    public String objectKey()
    {
        return adapterFactory.getObjectKey();
    }

    public Class<? extends T> typeForID(String id)
    {
        return adapterFactory.objectClass(id);
    }

    public String iDForType(Class<? extends T> type)
    {
        return adapterFactory.type(type);
    }

    public Collection<String> allIDs()
    {
        return adapterFactory.allIDs();
    }
}
