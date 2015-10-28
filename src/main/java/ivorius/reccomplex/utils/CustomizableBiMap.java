/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.Objects;

/**
 * Created by lukas on 28.10.15.
 */
public class CustomizableBiMap<K, V>
{
    private final BiMap<K, V> map = HashBiMap.create();
    private final BiMap<K, V> solidMap = HashBiMap.create();

    protected boolean hasCustom(K k)
    {
        V v = map.get(k);
        return v != null && !v.equals(solidMap.get(k));
    }

    protected boolean hasSolid(K k)
    {
        return solidMap.containsKey(k);
    }

    public void put(K k, V v, boolean custom)
    {
        if (custom)
        {
            V old = map.get(k);
            if (old != null)
                solidMap.put(k, old);

            map.put(k, v);
        }
        else
        {
            if (hasCustom(k))
                map.put(k, v);

            solidMap.put(k, v);
        }
    }

    public V remove(K k, boolean custom)
    {
        if (custom)
        {
            return hasCustom(k) ? map.put(k, solidMap.get(k)) : null;
        }
        else
        {
            if (!hasCustom(k))
                map.remove(k);

            return solidMap.remove(k);
        }
    }

    public void clearCustom()
    {
        map.clear();
        map.putAll(solidMap);
    }

    public BiMap<K, V> getMap()
    {
        return map;
    }

    public BiMap<K, V> getSolidMap()
    {
        return solidMap;
    }
}
