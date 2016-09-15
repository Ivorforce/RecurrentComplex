/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import com.google.common.collect.ImmutableMultimap;
import ivorius.ivtoolkit.tools.GuavaCollectors;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Created by lukas on 15.09.16.
 */
public class RCFunctions
{
    public static <T> Collection<Collection<T>> group(List<T> list, Function<T, Object> group)
    {
        return groupMap(list, group).asMap().values();
    }

    public static <K, T> ImmutableMultimap<K, T> groupMap(List<T> list, Function<T, K> group)
    {
        return list.stream().collect(GuavaCollectors.toMultimap(group, Collections::singleton));
    }
}
