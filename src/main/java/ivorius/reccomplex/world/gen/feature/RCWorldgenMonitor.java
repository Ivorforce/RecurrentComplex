/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature;

import ivorius.reccomplex.RecurrentComplex;

public class RCWorldgenMonitor
{
    public static String action;

    public static void create()
    {
        WorldgenMonitor.create("Recurrent Complex", (p, d) -> {
            if (action != null)
                RecurrentComplex.logger.warn("Cascading chunk generation happening while " + action);
        });
    }
}
