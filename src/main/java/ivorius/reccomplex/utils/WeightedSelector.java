/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import java.util.List;
import java.util.Random;

public class WeightedSelector<T>
{
    public static <T extends Item> T selectItem(Random rand, List<T> items)
    {
        double totalWeight = 0.0;
        for (T i : items)
            totalWeight += i.getWeight();
        return selectItem(rand, items, totalWeight);
    }

    public static <T extends Item> T selectItem(Random rand, List<T> items, double totalWeight)
    {
        double random = rand.nextDouble() * totalWeight;
        for (T item : items)
        {
            random -= item.getWeight();
            if (random <= 0.0)
                return item;
        }

        return items.get(0);
    }

    public static <T> T select(Random rand, List<SimpleItem<T>> items)
    {
        return selectItem(rand, items).getItem();
    }

    public static <T> T select(Random rand, List<SimpleItem<T>> items, double totalWeight)
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

