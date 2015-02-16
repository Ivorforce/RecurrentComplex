/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import java.util.List;
import java.util.Random;

public class WeightedSelector<T>
{
    public static <T> Item<T> selectItem(Random rand, List<Item<T>> items)
    {
        double totalWeight = 0.0;
        for (Item i : items)
            totalWeight += i.getWeight();
        return selectItem(rand, items, totalWeight);
    }

    public static <T> Item<T> selectItem(Random rand, List<Item<T>> items, double totalWeight)
    {
        double random = rand.nextDouble() * totalWeight;
        for (Item<T> item : items)
        {
            random -= item.getWeight();
            if (random <= 0.0)
                return item;
        }

        return items.get(0);
    }

    public static <T> T select(Random rand, List<Item<T>> items)
    {
        return selectItem(rand, items).getItem();
    }

    public static <T> T select(Random rand, List<Item<T>> items, double totalWeight)
    {
        return selectItem(rand, items, totalWeight).getItem();
    }

    public static class Item<T>
    {
        private final double weight;
        private final T item;

        public Item(double weight, T item)
        {
            this.item = item;
            this.weight = weight;
        }

        public T getItem()
        {
            return item;
        }

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

