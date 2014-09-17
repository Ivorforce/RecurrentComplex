/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.random;

import net.minecraft.util.WeightedRandom;

import java.util.*;

/**
 * Created by lukas on 17.09.14.
 */
public class LayeredStringGenerator
{
    public Layer baseLayer;

    public LayeredStringGenerator(Layer baseLayer)
    {
        this.baseLayer = baseLayer;
    }

    public String randomString(Random random)
    {
        return baseLayer.randomString(random);
    }

    public static interface Layer
    {
        String randomString(Random random);
    }

    public static class LayerSimple implements Layer
    {
        private Map<Character, Layer> subLayers;
        private List<WeightedString> baseStrings = new ArrayList<>();

        public LayerSimple(Map<Character, Layer> subLayers)
        {
            this.subLayers = subLayers;
        }

        public LayerSimple(Object... layersForCharacters)
        {
            this.subLayers = new HashMap<>();

            for (int i = 0; i < layersForCharacters.length / 2; i++)
            {
                this.subLayers.put((Character) layersForCharacters[i * 2], (Layer) layersForCharacters[i * 2 + 1]);
            }
        }

        public void addStrings(int weight, String... strings)
        {
            for (String s : strings)
                baseStrings.add(new WeightedString(weight, s));
        }

        @Override
        public String randomString(Random random)
        {
            StringBuilder stringBuilder = new StringBuilder();

            String component = ((WeightedString) WeightedRandom.getRandomItem(random, baseStrings)).string;
            for (char aChar : component.toCharArray())
            {
                Layer subLayer = subLayers.get(aChar);

                if (subLayer == null)
                    stringBuilder.append(aChar);
                else
                    stringBuilder.append(subLayer.randomString(random));
            }

            return stringBuilder.toString();
        }
    }

    public static class LayerStatic implements Layer
    {
        private List<WeightedString> baseStrings = new ArrayList<>();

        public LayerStatic(String... baseStrings)
        {
            addStrings(1, baseStrings);
        }

        public void addStrings(int weight, String... strings)
        {
            for (String s : strings)
                baseStrings.add(new WeightedString(weight, s));
        }

        @Override
        public String randomString(Random random)
        {
            return ((WeightedString) WeightedRandom.getRandomItem(random, baseStrings)).string;
        }
    }

    public static class LayerUppercase implements Layer
    {
        public Layer parent;

        public LayerUppercase(Layer parent)
        {
            this.parent = parent;
        }

        @Override
        public String randomString(Random random)
        {
            return firstCharUppercase(parent.randomString(random));
        }

        private static String firstCharUppercase(String name)
        {
            return Character.toString(name.charAt(0)).toUpperCase() + name.substring(1);
        }
    }

    private static <O> O getRandomElementFrom(List<O> list, Random random)
    {
        return list.get(random.nextInt(list.size()));
    }

    private static class WeightedString extends WeightedRandom.Item
    {
        public String string;

        private WeightedString(int weight, String string)
        {
            super(weight);
            this.string = string;
        }
    }
}
