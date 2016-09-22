/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import net.minecraft.util.math.BlockPos;

/**
 * Created by lukas on 22.09.16.
 */
public class RCAxisAlignedTransform
{
    public static BlockPos.MutableBlockPos apply(BlockPos.MutableBlockPos position, int[] size, AxisAlignedTransform2D transform)
    {
        return apply(position, position, size, transform);
    }

    public static BlockPos.MutableBlockPos apply(BlockPos position, BlockPos.MutableBlockPos onPosition, int[] size, AxisAlignedTransform2D transform)
    {
        boolean mirrorX = transform.isMirrorX();
        int rotation = transform.getRotation();

        int positionX = mirrorX ? size[0] - 1 - position.getX() : position.getX();

        switch (rotation)
        {
            case 0:
                return onPosition.setPos(positionX, position.getY(), position.getZ());
            case 1:
                return onPosition.setPos(size[2] - 1 - position.getZ(), position.getY(), positionX);
            case 2:
                return onPosition.setPos(size[0] - 1 - positionX, position.getY(), size[2] - 1 - position.getZ());
            case 3:
                return onPosition.setPos(position.getZ(), position.getY(), size[0] - 1 - positionX);
            default:
                throw new InternalError();
        }
    }
}
