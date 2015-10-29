/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Created by lukas on 28.10.15.
 */
public class CustomizableBiMap<K, V> extends CustomizableMap<K, V>
{
    public CustomizableBiMap()
    {
        super(HashBiMap.<K, V>create(), HashBiMap.<K, V>create());
    }

    @Override
    public BiMap<K, V> getMap()
    {
        return (BiMap<K, V>) map;
    }

    @Override
    public BiMap<K, V> getSolidMap()
    {
        return (BiMap<K, V>) solidMap;
    }
}
