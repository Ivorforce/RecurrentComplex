/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class WeightedSelector
{
    public static <T extends Item> T selectItem(Random rand, Collection<T> items)
    {
        double totalWeight = 0.0;
        for (T t : items)
            totalWeight += t.getWeight();
        return selectItem(rand, items, totalWeight);
    }

    public static <T extends Item> T selectItem(Random rand, Collection<T> items, double totalWeight)
    {
        if (items.size() == 0)
            throw new IndexOutOfBoundsException();

        double random = rand.nextDouble() * totalWeight;
        T last = null;
        for (T t : items)
        {
            last = t;
            random -= t.getWeight();
            if (random <= 0.0)
                return t;
        }

        return last;
    }

    public static <T> T select(Random rand, Collection<T> items, final Function<T, Double> weightFunction)
    {
        return select(rand, Collections2.transform(items, new Function<T, SimpleItem<T>>()
        {
            @Nullable
            @Override
            public SimpleItem<T> apply(T input)
            {
                return new SimpleItem<T>(weightFunction.apply(input), input);
            }
        }));
    }

    public static <T> T select(Random rand, Collection<SimpleItem<T>> items)
    {
        return selectItem(rand, items).getItem();
    }

    public static <T> T select(Random rand, Collection<SimpleItem<T>> items, double totalWeight)
    {
        return selectItem(rand, items, totalWeight).getItem();
    }

    public static interface Item
    {
        double getWeight();
    }

    public static class SimpleItem<T> implements Item
    {
        private final double weight;
        private final T item;

        public SimpleItem(double weight, T item)
        {
            this.item = item;
            this.weight = weight;
        }

        public T getItem()
        {
            return item;
        }

        @Override
        public double getWeight()
        {
            return weight;
        }

        @Override
        public String toString()
        {
            return String.format("{\"_class\": WeightedItem {\"weight\":\"%f\", \"item\":\"%s}", weight, item);
        }
    }
}

