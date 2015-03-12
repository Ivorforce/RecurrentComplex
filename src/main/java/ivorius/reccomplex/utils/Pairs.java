/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;

/**
 * Created by lukas on 12.03.15.
 */
public class Pairs
{
    public static <L, R> Iterable<Pair<L, R>> pairLeft(final L left, Iterable<R> right)
    {
        return Iterables.transform(right, new Function<R, Pair<L, R>>()
        {
            @Nullable
            @Override
            public Pair<L, R> apply(R input)
            {
                return Pair.of(left, input);
            }
        });
    }

    public static <L, R> Iterable<Pair<L, R>> pairRight(Iterable<L> left, final R right)
    {
        return Iterables.transform(left, new Function<L, Pair<L, R>>()
        {
            @Nullable
            @Override
            public Pair<L, R> apply(L input)
            {
                return Pair.of(input, right);
            }
        });
    }
}
