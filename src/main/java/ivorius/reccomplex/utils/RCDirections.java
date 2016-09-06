/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

/**
 * Created by lukas on 06.09.16.
 */
public class RCDirections
{
    @Nullable
    public static Integer getHorizontalClockwiseRotations(EnumFacing source, EnumFacing dest, boolean mirrorX)
    {
        if (source == dest)
            return mirrorX && dest.getAxis() == EnumFacing.Axis.X ? 2 : 0;

        int horizontalIndexSrc = source.getHorizontalIndex();
        int horizontalIndexDst = dest.getHorizontalIndex();

        if (horizontalIndexSrc >= 0 && horizontalIndexDst >= 0)
        {
            int mirrorRotations = mirrorX && dest.getAxis() == EnumFacing.Axis.X ? 2 : 0;
            return ((horizontalIndexDst - horizontalIndexSrc + mirrorRotations) + EnumFacing.HORIZONTALS.length) % EnumFacing.HORIZONTALS.length;
        }

        return null;
    }
}
