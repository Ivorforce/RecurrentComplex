/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.nbt;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.ivtoolkit.tools.NBTCompoundObjects;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Created by lukas on 21.03.16.
 */
public class NBTStringTypeRegistry<B extends NBTCompoundObject>
{
    private BiMap<Class<? extends B>, String> classMap = HashBiMap.create();

    private String typeKey;
    private String objectKey;

    public NBTStringTypeRegistry(String objectKey, String typeKey)
    {
        if (objectKey.equals(typeKey))
            throw new IllegalArgumentException("Type key must be different from object key");

        this.objectKey = objectKey;
        this.typeKey = typeKey;
    }

    public NBTStringTypeRegistry()
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

    public <T extends B> void register(String id, Class<? extends T> clazz)
    {
        classMap.put(clazz, id);
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
        return classMap.inverse().keySet();
    }

    public B copy(B b)
    {
        return read(write(b));
    }

    public NBTTagCompound write(B t)
    {
        NBTTagCompound compound = new NBTTagCompound();
        //noinspection unchecked
        compound.setString(getTypeKey(), type((Class<? extends B>) t.getClass()));
        compound.setTag(getObjectKey(), NBTCompoundObjects.write(t));
        return compound;
    }

    @Nullable
    public B read(NBTTagCompound compound)
    {
        Class<? extends B> type = objectClass(compound.getString(getTypeKey()));
        return type != null ? NBTCompoundObjects.read(compound.getCompoundTag(getObjectKey()), () -> ivorius.ivtoolkit.lang.IvClasses.instantiate(type)) : null;
    }
}
