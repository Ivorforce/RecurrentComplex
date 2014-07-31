/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import net.minecraftforge.common.config.Configuration;

/**
 * Created by lukas on 31.07.14.
 */
public class RCConfig
{
    public static boolean generateDefaultStructures;

    public static void loadConfig(String configID)
    {
        if (configID == null || configID.equals(Configuration.CATEGORY_GENERAL))
        {
            generateDefaultStructures = RecurrentComplex.config.getBoolean("generateDefaultStructures", Configuration.CATEGORY_GENERAL, true, "Generate the default mod set of structures?");
        }

        RecurrentComplex.proxy.loadConfig(configID);
    }
}
