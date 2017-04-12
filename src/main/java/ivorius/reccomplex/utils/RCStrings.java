/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 12.04.17.
 */
public class RCStrings
{
    @Nonnull
    public static String abbreviateFormatted(String string, int offset, int maxWidth)
    {
        String abbreviate = StringUtils.abbreviate(string, offset, maxWidth);

        if (offset > 0 && abbreviate.startsWith("...") && abbreviate.length() > 3 )
        {
            char first = abbreviate.charAt(3);
            abbreviate = first == '§' ? abbreviate : ("..." + abbreviate.substring(4)); // Cut off one char to avoid destroying §
        }

        if (abbreviate.endsWith("..."))
        {
            char last = abbreviate.charAt(abbreviate.length() - 4);
            abbreviate = abbreviate.substring(0, abbreviate.length() - (last == '§' ? 4 : 3)) + TextFormatting.RESET + "...";
        }

        return abbreviate;
    }

    public static String abbreviateFormatted(String string, int maxWidth)
    {
        return abbreviateFormatted(string, 0, maxWidth);
    }
}
