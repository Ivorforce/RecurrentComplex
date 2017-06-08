/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.mcopts.translation;

import net.minecraft.util.text.translation.I18n;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 08.06.17.
 */
@SuppressWarnings("deprecation")
public class Translations
{
    public static boolean has(String key)
    {
        return I18n.canTranslate(key);
    }

    public static String get(String key)
    {
        return I18n.translateToLocal(key);
    }

    public static String format(String key, Object... args)
    {
        return I18n.translateToLocalFormatted(key, args);
    }

    public static List<String> getLines(String key)
    {
        return splitLines(I18n.translateToLocal(key));
    }

    public static List<String> formatLines(String key, Object... args)
    {
        return splitLines(I18n.translateToLocalFormatted(key, args));
    }


    public static List<String> splitLines(String text)
    {
        return Arrays.asList(text.split("<br>"));
    }
}