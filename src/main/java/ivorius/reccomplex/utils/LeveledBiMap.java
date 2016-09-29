/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 28.10.15.
 */
public class LeveledBiMap<K, V> extends LeveledMap<K, V>
{
    public LeveledBiMap(int levels)
    {
        super(HashBiMap.create(), IntStream.range(0, levels).mapToObj(i -> HashBiMap.<K, V>create()).collect(Collectors.toList()));
    }

    @Override
    public BiMap<K, V> getMap()
    {
        return (BiMap<K, V>) map;
    }

    @Override
    public BiMap<K, V> getMap(int level)
    {
        return (BiMap<K, V>) super.getMap(level);
    }
}
