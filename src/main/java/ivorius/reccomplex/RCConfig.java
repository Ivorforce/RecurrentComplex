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

    public static boolean hideRedundantNegativeSpace;

    public static float structureSpawnChanceModifier = 1.0f;

    public static void loadConfig(String configID)
    {
        if (configID == null || configID.equals(Configuration.CATEGORY_GENERAL))
        {
            generateDefaultStructures = RecurrentComplex.config.getBoolean("generateDefaultStructures", Configuration.CATEGORY_GENERAL, true, "Generate the default mod set of structures?");
            structureSpawnChanceModifier = RecurrentComplex.config.getFloat("structureSpawnChance", Configuration.CATEGORY_GENERAL, 1.0f, 0.0f, 10.0f, "How often do structures spawn?");
        }

        RecurrentComplex.proxy.loadConfig(configID);
    }
}
