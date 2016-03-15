/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by lukas on 15.03.16.
 */
public class CustomizableSet<V> extends CustomizableObject<Set<V>>
{
    public CustomizableSet()
    {
        super(Sets.<V>newHashSet(), Sets.<V>newHashSet());
    }

    public CustomizableSet(Set<V> solid, Set<V> custom)
    {
        super(solid, custom);
    }

    public void setContains(boolean contains, boolean custom, V v)
    {
        if (contains)
            get(custom).add(v);
        else
            get(custom).remove(v);
    }
}
