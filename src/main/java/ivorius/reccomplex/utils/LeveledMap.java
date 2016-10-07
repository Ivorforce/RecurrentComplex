/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 28.10.15.
 */
public class LeveledMap<K, V>
{
    protected final Map<K, V> map;
    protected final TObjectIntMap<K> levelMap = new TObjectIntHashMap<>();
    protected final List<Map<K, V>> levels = new ArrayList<>();

    public LeveledMap(Map<K, V> map, Collection<? extends Map<K, V>> levels)
    {
        this.map = map;
        this.levels.addAll(levels);
    }

    public LeveledMap(int levels)
    {
        this(new HashMap<>(), IntStream.range(0, levels).mapToObj(i -> new HashMap<K, V>()).collect(Collectors.toList()));
    }

    public int levels()
    {
        return levels.size();
    }

    public boolean has(K k, int level)
    {
        return levels.get(level).containsKey(k);
    }

    public V put(K k, V v, int level)
    {
        if (level >= levels() || level < 0)
            throw new IndexOutOfBoundsException();

        V prev = levels.get(level).put(k, v);

        if (level >= levelMap.get(k))
        {
            map.put(k, v);
            levelMap.put(k, level);
        }

        return prev;
    }

    public V remove(K k, int level)
    {
        if (level >= levels() || level < 0)
            throw new IndexOutOfBoundsException();

        V prev = levels.get(level).remove(k);

        if (level == levelMap.get(k))
            putFirst(k, level);

        return prev;
    }

    private boolean putFirst(K k, int startLevel)
    {
        while(--startLevel >= 0)
        {
            V next = levels.get(startLevel).get(k);
            if (next != null)
            {
                map.put(k, next);
                levelMap.put(k, startLevel);
                return true;
            }
        }

        map.remove(k);
        levelMap.remove(k);

        return false;
    }

    public void clear()
    {
        map.clear();
        levelMap.clear();
        levels.forEach(Map::clear);
    }

    public void clear(int level)
    {
        if (level >= levels() || level < 0)
            throw new IndexOutOfBoundsException();

        levels.get(level).clear();
        calculateMap();
    }

    private void calculateMap()
    {
        map.clear();
        levelMap.clear();
        for (int l = 0; l < levels(); l++)
        {
            map.putAll(levels.get(l));
            for (K k : levels.get(l).keySet())
                levelMap.put(k, l);
        }
    }

    public Map<K, V> getMap()
    {
        return map;
    }

    public Map<K, V> getMap(int level)
    {
        return levels.get(level);
    }
}
