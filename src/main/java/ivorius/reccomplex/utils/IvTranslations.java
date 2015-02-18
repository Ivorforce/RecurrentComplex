/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.util.StatCollector;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 17.02.15.
 */
public class IvTranslations
{
    public static List<String> formatLines(String key, Object... args)
    {
        String translation = StatCollector.translateToLocalFormatted(key, args);
        return Arrays.asList(translation.split("<br>"));
    }
}
