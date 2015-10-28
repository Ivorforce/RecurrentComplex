/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by lukas on 28.10.15.
 */
public class CustomizableMap<K, V>
{
    private final Map<K, V> map = new HashMap<>();
    private final Map<K, V> solidMap = new HashMap<>();

    public void putSolid(K k, V v)
    {
        if (Objects.equals(map.get(k), solidMap.get(k)))
            map.put(k, v);

        solidMap.put(k, v);
    }

    public void putCustom(K k, V v)
    {
        V old = map.get(k);
        if (old != null)
            solidMap.put(k, old);

        map.put(k, v);
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
