/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import gnu.trove.map.TFloatObjectMap;
import gnu.trove.map.hash.TFloatObjectHashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lukas on 11.03.15.
 */
public class PrecedenceSets
{
    public static <T extends PrecedenceSet.NativeEntry> void add(Collection<PrecedenceSet<T>> collection, T t)
    {
        add(collection, t, t.getPrecedence());
    }

    public static <T> void add(Collection<PrecedenceSet<T>> collection, T t, float precedence)
    {
        for (PrecedenceSet<T> set : collection)
        {
            if (set.precedence == precedence)
            {
                set.getSet().add(t);
                return;
            }
        }

        collection.add(new PrecedenceSet<>(precedence, new HashSet<>(Collections.singleton(t))));
    }

    public static <T extends PrecedenceSet.NativeEntry> Set<PrecedenceSet<T>> group(Collection<T> ts)
    {
        TFloatObjectMap<PrecedenceSet<T>> map = new TFloatObjectHashMap<>();

        for (T t : ts)
        {
            float precedence = t.getPrecedence();

            PrecedenceSet<T> precedenceSet = map.get(precedence);
            if (precedenceSet == null)
                map.put(precedence, precedenceSet = new PrecedenceSet<>(precedence, new HashSet<T>()));

            precedenceSet.getSet().add(t);
        }

        return new HashSet<>(map.valueCollection());
    }

    public static <T> Set<PrecedenceSet<T>> group(Collection<T> ts, Function<T, Float> mapper)
    {
        TFloatObjectMap<PrecedenceSet<T>> map = new TFloatObjectHashMap<>();

        for (T t : ts)
        {
            float precedence = mapper.apply(t);

            PrecedenceSet<T> precedenceSet = map.get(precedence);
            if (precedenceSet == null)
                map.put(precedence, precedenceSet = new PrecedenceSet<>(precedence, new HashSet<T>()));

            precedenceSet.getSet().add(t);
        }

        return new HashSet<>(map.valueCollection());
    }
}
