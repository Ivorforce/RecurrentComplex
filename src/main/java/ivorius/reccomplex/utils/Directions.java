/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;

import static net.minecraftforge.common.util.ForgeDirection.*;

/**
 * Created by lukas on 03.03.15.
 */
public class Directions
{
    public static final ForgeDirection[] HORIZONTAL = new ForgeDirection[]{NORTH, EAST, SOUTH, WEST};
    public static final ForgeDirection[] X_AXIS = new ForgeDirection[]{EAST, WEST};
    public static final ForgeDirection[] Y_AXIS = new ForgeDirection[]{UP, DOWN};
    public static final ForgeDirection[] Z_AXIS = new ForgeDirection[]{SOUTH, NORTH};

    public static String displayName(ForgeDirection direction)
    {
        return StatCollector.translateToLocal("reccomplex.direction." + (direction == null ? "none" : serialize(direction).toLowerCase()));
    }

    @Nullable
    public static Integer getHorizontalClockwiseRotations(ForgeDirection source, ForgeDirection dest, boolean mirrorX)
    {
        if (source == dest)
            return mirrorX && ArrayUtils.contains(Directions.X_AXIS, dest) ? 2 : 0;

        int arrayIndexSrc = ArrayUtils.indexOf(HORIZONTAL, source);
        int arrayIndexDst = ArrayUtils.indexOf(HORIZONTAL, dest);

        if (arrayIndexSrc >= 0 && arrayIndexDst >= 0)
        {
            int mirrorRotations = mirrorX && ArrayUtils.contains(Directions.X_AXIS, dest) ? 2 : 0;
            return ((arrayIndexSrc - arrayIndexDst + mirrorRotations) + HORIZONTAL.length) % HORIZONTAL.length;
        }

        return null;
    }

    public static ForgeDirection rotate(ForgeDirection direction, AxisAlignedTransform2D transform)
    {
        if (direction == UP || direction == DOWN)
            return direction;

        int rotations = transform.getRotation();
        if (transform.isMirrorX() && ArrayUtils.contains(X_AXIS, direction))
            rotations += 2;
        return HORIZONTAL[(ArrayUtils.indexOf(HORIZONTAL, direction) + rotations) % HORIZONTAL.length];
    }

    public static ForgeDirection deserialize(String id)
    {
        ForgeDirection direction = IvGsonHelper.enumForNameIgnoreCase(id, values());
        return direction != null ? direction : NORTH;
    }

    public static ForgeDirection deserializeHorizontal(String id)
    {
        ForgeDirection direction = IvGsonHelper.enumForNameIgnoreCase(id, HORIZONTAL);
        return direction != null ? direction : NORTH;
    }

    public static String serialize(ForgeDirection direction)
    {
        return IvGsonHelper.serializedName(direction);
    }

    public static ForgeDirection getDirectionFromVRotation(int front)
    {
        switch (front)
        {
            default:
            case 0:
                return NORTH;
            case 1:
                return EAST;
            case 2:
                return SOUTH;
            case 3:
                return WEST;
        }
    }
}
