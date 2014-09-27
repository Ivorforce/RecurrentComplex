/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.json;

import ivorius.ivtoolkit.tools.IvGsonHelper;

/**
 * Created by lukas on 27.09.14.
 */
public class RCGsonHelper
{
    public static <E extends Enum> E enumForNameIgnoreCase(String serializedName, E[] values)
    {
        for (E anEnum : values)
        {
            if (IvGsonHelper.serializedName(anEnum).equalsIgnoreCase(serializedName))
                return anEnum;
        }

        return null;
    }
}
