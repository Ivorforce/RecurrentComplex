/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.rendering.Icon;
import net.minecraft.util.math.MathHelper;

import java.util.List;

/**
 * Created by lukas on 23.03.15.
 */
public class Icons
{
    public static final Icon WHOLE_TEXTURE = Icon.fromCoords(0, 1, 0, 1);

    public static Icon from(final float minU, final float minV, final float maxU, final float maxV)
    {
        return Icon.fromCoords(minU, maxU, minV, maxV);
    }

    public static <T> T frame(T[] icons, float ticks)
    {
        return icons[(MathHelper.floor_float(ticks) % icons.length + icons.length) % icons.length];
    }

    public static <T> T frame(List<T> icons, float ticks)
    {
        return icons.get((MathHelper.floor_float(ticks) % icons.size() + icons.size()) % icons.size());
    }
}
