/*
 * Copyright (c) 2014, Lukas Tenbrink.
 * http://lukas.axxim.net
 *
 * You are free to:
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes, unless you have a permit by the creator.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package ivorius.structuregen.ivtoolkit;

import java.util.Random;

public class IvStringHelper
{
    public static String cheeseString(String string, float effect, long seed)
    {
        return cheeseString(string, effect, new Random(seed));
    }

    public static String cheeseString(String string, float effect, Random rand)
    {
        if (effect <= 0.0f)
        {
            return string;
        }

        StringBuilder builder = new StringBuilder(string.length());

        for (int i = 0; i < string.length(); i++)
        {
            if (rand.nextFloat() <= effect)
            {
                builder.append(' ');
            }
            else
            {
                builder.append(string.charAt(i));
            }
        }

        return builder.toString();
    }

    public static int countOccurrences(String haystack, char needle)
    {
        int count = 0;

        for (int i = 0; i < haystack.length(); i++)
        {
            if (haystack.charAt(i) == needle)
            {
                count++;
            }
        }

        return count;
    }
}
