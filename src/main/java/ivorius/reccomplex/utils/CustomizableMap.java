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
    protected final Map<K, V> solidMap;

    protected CustomizableMap(Map<K, V> map, Map<K, V> solidMap)
    {
        this.map = map;
        this.solidMap = solidMap;
    }

    public CustomizableMap()
    {
        this(new HashMap<>(), new HashMap<>());
    }

    public boolean hasCustom(K k)
    {
        V v = map.get(k);
        return v != null && !v.equals(solidMap.get(k));
    }

    public boolean hasSolid(K k)
    {
        return solidMap.containsKey(k);
    }

    public V put(K k, V v, boolean custom)
    {
        if (custom)
            return map.put(k, v);
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
            return hasCustom(k) ? map.put(k, solidMap.get(k)) : null;
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

    public Map<K, V> getMap()
    {
        return map;
    }

    public Map<K, V> getSolidMap()
    {
        return solidMap;
    }
}
