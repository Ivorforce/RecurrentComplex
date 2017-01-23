/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.blocks.Directions;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;

/**
 * Created by lukas on 23.01.17.
 */
public class RCDirections
{
     // TODO Transfer fix to IvToolkit
    @Nullable
    public static Integer getHorizontalClockwiseRotations(EnumFacing source, EnumFacing dest, boolean mirrorX)
    {
        if (source == dest)
            return mirrorX && ArrayUtils.contains(Directions.X_AXIS, dest) ? 2 : 0;

        int arrayIndexSrc = ArrayUtils.indexOf(Directions.HORIZONTAL, source);
        int arrayIndexDst = ArrayUtils.indexOf(Directions.HORIZONTAL, dest);

        if (arrayIndexSrc >= 0 && arrayIndexDst >= 0)
        {
//            int mirrorRotations = mirrorX && ArrayUtils.contains(Directions.X_AXIS, dest) ? 2 : 0;
            int mirrorRotations = mirrorX && ArrayUtils.contains(Directions.X_AXIS, source) ? 2 : 0;
            return ((arrayIndexDst - arrayIndexSrc + mirrorRotations)
                    + Directions.HORIZONTAL.length) % Directions.HORIZONTAL.length;
        }

        return null;
    }
}
