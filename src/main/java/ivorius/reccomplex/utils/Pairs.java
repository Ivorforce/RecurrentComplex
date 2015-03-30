/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

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

    public static <L, R> List<Pair<L, R>> of(List<L> left, List<R> right)
    {
        return new PairList<>(left, right);
    }

    protected static class PairList<L, R> extends AbstractList<Pair<L, R>>
    {
        protected List<L> left;
        protected List<R> right;

        public PairList(List<L> left, List<R> right)
        {
            this.left = left;
            this.right = right;
        }

        @Override
        public Pair<L, R> get(int index)
        {
            return Pair.of(left.get(index), right.get(index));
        }

        @Override
        public int size()
        {
            return Math.min(left.size(), right.size());
        }
    }
}
