/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.ivtoolkit.logic;

import java.util.*;

/**
 * Created by lukas on 28.07.14.
 */
public class ReferenceCounter<V>
{
    private Map<V, Integer> map;
    private Set<V> freeObjects;

    public ReferenceCounter()
    {
        this.map = new HashMap<>();
        freeObjects = new HashSet<>();
    }

    public int getRetainCount(V object)
    {
        Integer val = map.get(object);
        return val != null ? val : 0;
    }

    public boolean hasObject(V object)
    {
        return getRetainCount(object) > 0;
    }

    public void retain(V object, int retainCount)
    {
        int newCount = getRetainCount(object) + retainCount;
        map.put(object, newCount);
    }

    public boolean release(V object, int releaseCount)
    {
        int newCount = getRetainCount(object) - releaseCount;

        if (newCount < 0)
            throw new RuntimeException("Trying to release a freed object!");

        if (newCount == 0)
        {
            freeObjects.add(object);
            map.remove(object);
        }
        else
        {
            map.put(object, newCount);
        }

        return newCount > 0;
    }

    public Set<V> deallocateAllFreeObjects()
    {
        if (freeObjects.size() == 0)
            return Collections.emptySet();

        Set<V> freeObjects = this.freeObjects;
        this.freeObjects = new HashSet<>();
        return freeObjects;
    }

    public int numberOfRetainedObjects()
    {
        return map.size();
    }

    public int numberOfFreeObjects()
    {
        return freeObjects.size();
    }

    public Set<V> getFreeObjects()
    {
        return Collections.unmodifiableSet(freeObjects);
    }

    public Set<V> getAllRetainedObjects()
    {
        return map.keySet();
    }
}
