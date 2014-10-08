/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.gui.FloatRange;
import ivorius.ivtoolkit.gui.IntegerRange;
import net.minecraft.util.MathHelper;

/**
 * Created by lukas on 08.10.14.
 */
public class RangeHelper
{
    public static IntegerRange roundedIntRange(FloatRange floatRange)
    {
        return new IntegerRange(MathHelper.floor_float(floatRange.getMin() + 0.5f), MathHelper.floor_float(floatRange.getMax() + 0.5f));
    }
}
