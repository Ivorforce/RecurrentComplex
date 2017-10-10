/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 12.04.17.
 */
public class RCStrings
{
    /**
     * From StringUtils
     */
    private static MutablePair<Integer, Integer> abbreviate(final String str, int offset, final int maxWidth)
    {
        if (str == null)
        {
            return null;
        }
        if (maxWidth < 4)
        {
            throw new IllegalArgumentException("Minimum abbreviation width is 4");
        }
        if (str.length() <= maxWidth)
        {
            return MutablePair.of(0, str.length());
        }
        if (offset > str.length())
        {
            offset = str.length();
        }
        if (str.length() - offset < maxWidth - 3)
        {
            offset = str.length() - (maxWidth - 3);
        }
        if (offset <= 4)
        {
            return MutablePair.of(0, maxWidth - 3);
        }
        if (maxWidth < 7)
        {
            throw new IllegalArgumentException("Minimum abbreviation width with offset is 7");
        }
        if (offset + maxWidth - 3 < str.length())
        {
            Pair<Integer, Integer> abbreviated = abbreviate(str.substring(offset), 0, maxWidth - 3);
            return MutablePair.of(offset, offset + abbreviated.getRight());
        }
        return MutablePair.of(str.length() - (maxWidth - 3), str.length());
    }

    @Nonnull
    public static String abbreviateFormatted(String str, int offset, int maxWidth)
    {
        MutablePair<Integer, Integer> abbreviate = abbreviate(str, offset, maxWidth);
        String abbreviated = str.substring(abbreviate.getLeft(), abbreviate.getRight());

        if (abbreviate.getLeft() > 0)
        {
            // Get the current format back in
            String prevFormat = "";
            if (str.length() > abbreviate.getLeft())
                for (int i = abbreviate.getLeft() - 1; i >= 0; i--)
                {
                    char fst = str.charAt(i);
                    char scd = str.charAt(i + 1);
                    if (fst == '§' && scd != '§')
                        prevFormat = fst + (scd + prevFormat);
                }

            if (str.charAt(abbreviate.getLeft() - 1) == '§')
                abbreviated = abbreviated.substring(1); // Cut off format char

            abbreviated = "..." + prevFormat + abbreviated;
        }

        if (abbreviate.getRight() < str.length())
        {
            if (str.charAt(abbreviate.getRight() - 1) == '§')
                abbreviated = abbreviated.substring(0, abbreviated.length() - 1); // Cut off format char

            abbreviated = abbreviated + TextFormatting.RESET + "...";
        }

        return abbreviated;
    }

    public static String abbreviateFormatted(String str, int maxWidth)
    {
        return abbreviateFormatted(str, 0, maxWidth);
    }

    public static Long seed(String seed)
    {
        if (seed == null || StringUtils.isEmpty(seed))
            return null;

        try
        {
            long j = Long.parseLong(seed);

            if (j != 0L)
                return j;
        }
        catch (NumberFormatException var7)
        {
            return (long) seed.hashCode();
        }

        return null;
    }

    public static String shorten(String string, FontRenderer renderer, int width)
    {
        boolean shortened = false;
        int stringWidth;
        while ((stringWidth = renderer.getStringWidth(string)) > width)
        {
            if (!shortened)
            {
                shortened = true;
                string = "..." + string;
            }

            string = string.substring(0, string.length() - 1);
        }
        return shortened ? string.substring(3) + TextFormatting.RESET + "..." : string;
    }
}
