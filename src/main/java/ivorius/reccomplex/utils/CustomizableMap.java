/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lukas on 28.10.15.
 */
public class CustomizableMap<K, V>
{
    protected final Map<K, V> map;
    protected final Map<K, V> customMap;
    protected final Map<K, V> solidMap;

    public CustomizableMap(Map<K, V> map, Map<K, V> customMap, Map<K, V> solidMap)
    {
        this.map = map;
        this.customMap = customMap;
        this.solidMap = solidMap;
    }

    public CustomizableMap()
    {
        this(new HashMap<>(), new HashMap<K, V>(), new HashMap<>());
    }

    public boolean hasCustom(K k)
    {
        return customMap.containsKey(k);
    }

    public boolean hasSolid(K k)
    {
        return solidMap.containsKey(k);
    }

    public V put(K k, V v, boolean custom)
    {
        if (custom)
        {
            customMap.put(k, v);
            return map.put(k, v);
        }
        else
        {
            if (!hasCustom(k))
                map.put(k, v);

            return solidMap.put(k, v);
        }
    }

    public V remove(K k, boolean custom)
    {
        if (custom)
        {
            customMap.remove(k);

            if (hasSolid(k))
                return map.put(k, solidMap.get(k));
            else
                return map.remove(k);
        }
        else
        {
            if (!hasCustom(k))
                map.remove(k);

            return solidMap.remove(k);
        }
    }

    public void clear()
    {
        map.clear();
        solidMap.clear();
        customMap.clear();
    }

    public void clearCustom()
    {
        map.clear();
        customMap.clear();
        map.putAll(solidMap);
    }

    public void clearSolid()
    {
        map.clear();
        solidMap.clear();
        map.putAll(customMap);
    }

    public Map<K, V> getMap()
    {
        return map;
    }

    public Map<K, V> getCustomMap()
    {
        return customMap;
    }

    public Map<K, V> getSolidMap()
    {
        return solidMap;
    }
}
