/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.blocks.Directions;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.EnumFacing;

/**
 * Created by lukas on 30.03.15.
 */
public class DirectionNames
{
    public static String of(EnumFacing direction)
    {
        return of(direction, "none");
    }

    public static String of(EnumFacing direction, String nullTitle)
    {
        return I18n.translateToLocal("reccomplex.direction." + (direction == null ? nullTitle : Directions.serialize(direction).toLowerCase()));
    }
}
