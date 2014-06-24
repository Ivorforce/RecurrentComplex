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

package ivorius.structuregen.ivtoolkit.tools;

import java.util.Calendar;

import static java.util.Calendar.*;

/**
 * Created by lukas on 01.05.14.
 */
public class IvDateHelper
{
    public static boolean isHalloween()
    {
        Calendar cal = getInstance();

        return cal.get(MONTH) == OCTOBER && cal.get(DAY_OF_MONTH) == 31;
    }

    public static boolean isChristmas()
    {
        Calendar cal = getInstance();

        int day = cal.get(DAY_OF_MONTH);
        return cal.get(MONTH) == DECEMBER && day == 23 || day == 24;
    }

    public static boolean isAprilFools()
    {
        Calendar cal = getInstance();

        return cal.get(MONTH) == APRIL && cal.get(DAY_OF_MONTH) == 1;
    }

    public static boolean isMardiGras()
    {
        Calendar cal = getInstance();

        return cal.get(MONTH) == MARCH && cal.get(DAY_OF_MONTH) == 4;
    }

    public static boolean isOppositeDay()
    {
        Calendar cal = getInstance();

        return cal.get(MONTH) == JANUARY && cal.get(DAY_OF_MONTH) == 25;
    }
}
