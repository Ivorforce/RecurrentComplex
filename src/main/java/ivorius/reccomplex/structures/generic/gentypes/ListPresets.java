/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by lukas on 26.02.15.
 */
public abstract class ListPresets<T>
{
    protected Gson gson = createGson();

    protected final Map<String, List<T>> presets = new HashMap<>();

    @Nullable
    protected String defaultType;

    public void register(@Nonnull String type, @Nonnull List<T> values)
    {
        presets.put(type, Lists.newArrayList(values));
    }

    @SafeVarargs
    public final void register(@Nonnull String type, @Nonnull T... values)
    {
        presets.put(type, Arrays.asList(values));
    }

    public void setDefault(@Nonnull String type)
    {
        defaultType = type;
    }

    @Nullable
    public String defaultType()
    {
        return defaultType;
    }

    @Nullable
    public List<T> preset(String type)
    {
        List<T> list = presets.get(type);
        if (list != null)
            return Arrays.asList(gson.fromJson(gson.toJsonTree(list), getType()));
        return null;
    }

    public Collection<String> allTypes()
    {
        return presets.keySet();
    }

    public boolean has(String type)
    {
        return presets.containsKey(type);
    }

    protected abstract Gson createGson();

    protected abstract Class<T[]> getType();
}
