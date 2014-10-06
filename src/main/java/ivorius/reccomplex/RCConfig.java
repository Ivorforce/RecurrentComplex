/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import net.minecraftforge.common.config.Configuration;

import java.util.*;

/**
 * Created by lukas on 31.07.14.
 */
public class RCConfig
{
    public static boolean generateDefaultStructures;

    public static boolean hideRedundantNegativeSpace;

    public static float structureSpawnChanceModifier = 1.0f;
    private static Set<String> disabledStructures = new HashSet<>();
    private static Set<String> persistentDisabledStructures = new HashSet<>();
    private static Set<String> forceEnabledStructures = new HashSet<>();

    public static void loadConfig(String configID)
    {
        if (configID == null || configID.equals(Configuration.CATEGORY_GENERAL))
        {
            generateDefaultStructures = RecurrentComplex.config.getBoolean("generateDefaultStructures", Configuration.CATEGORY_GENERAL, true, "Generate the default mod set of structures?");
            structureSpawnChanceModifier = RecurrentComplex.config.getFloat("structureSpawnChance", Configuration.CATEGORY_GENERAL, 1.0f, 0.0f, 10.0f, "How often do structures spawn?");

            disabledStructures.clear();
            disabledStructures.addAll(Arrays.asList(RecurrentComplex.config.getStringList("disabledStructures", Configuration.CATEGORY_GENERAL, new String[0], "Structures that will be hindered from generating")));
            forceEnabledStructures.clear();
            forceEnabledStructures.addAll(Arrays.asList(RecurrentComplex.config.getStringList("forceEnabledStructures", Configuration.CATEGORY_GENERAL, new String[0], "Structures that be set to generate (if in the right directory), no matter what")));
        }

        RecurrentComplex.proxy.loadConfig(configID);
    }

    public static void setStructurePersistentlyDisabled(String id, boolean disabled)
    {
        if (disabled)
            persistentDisabledStructures.add(id);
    }

    public static boolean isStructureDisabled(String id)
    {
        return !forceEnabledStructures.contains(id)
                && (persistentDisabledStructures.contains(id) || disabledStructures.contains(id));
    }
}
