/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.random;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 17.06.14.
 */
public class Place
{
    private static List<String> placeTypes = Arrays.asList("hill", "mountain", "village", "town", "sea", "forest", "cave", "field", "tower", "dungeon", "realm", "dimension", "temple", "shrine", "hut", "shack", "cottage", "cavern", "crevice", "ruïns", "lowland", "plain", "peak", "cliff", "creek", "meadow", "tower", "lighthouse", "mansion", "sewer", "cellar", "church", "valley", "ravine", "ridge", "glacier", "river", "lake", "waterfall", "island", "isle", "reef");
    private static List<String> placeTraits = Arrays.asList("luscious", "heavenly", "lush", "dense", "palatial", "firey", "blazing", "hot", "mystical", "arcane", "anagogic", "hermetical", "orphic", "magical", "vast", "boundless", "colossal", "enormous", "extensive", "immensive", "monumental", "tremendous", "eternal", "pristine", "immaculate", "cold", "snowy", "frigid", "frozen", "frosty", "glacial", "brobdingnagian", "gamol", "icey", "barren", "deserted", "rotten", "undead", "cursed", "godlike", "vibrant", "doomed", "sacred", "divine");

    private String placeType;
    private String placeTrait;
    private String placeName;

    public Place(String placeType, String placeTrait, String placeName)
    {
        this.placeType = placeType;
        this.placeTrait = placeTrait;
        this.placeName = placeName;
    }

    public static Place randomPlace(Random random)
    {
        String placeType = getRandomElementFrom(placeTypes, random) + (random.nextFloat() < 0.6f ? "s" : "");
        String placeTrait = random.nextFloat() < 0.9f ? getRandomElementFrom(placeTraits, random) : null;
        String placeName = null;

        if (random.nextFloat() < 0.6f)
        {
            switch (random.nextInt(4))
            {
                case 0:
                    placeName = "of ";
                    break;
                case 1:
                    placeName = "at ";
                    break;
                case 2:
                    placeName = "from ";
                    break;
                case 3:
                    placeName = " ";
                    break;
            }

            placeName += Person.chaoticName(random, random.nextBoolean());
        }

        return new Place(placeType, placeTrait, placeName);
    }

    private static <O> O getRandomElementFrom(List<O> list, Random random)
    {
        return list.get(random.nextInt(list.size()));
    }

    public String getPlaceType()
    {
        return placeType;
    }

    public String getPlaceTrait()
    {
        return placeTrait;
    }

    public String getPlaceName()
    {
        return placeName;
    }

    public String getFullPlaceType()
    {
        return placeTrait != null ? placeTrait + " " +  placeType: placeType;
    }

    public String getFullName()
    {
        return placeName != null ? getFullPlaceType() + " " + placeName : getFullPlaceType();
    }
}
