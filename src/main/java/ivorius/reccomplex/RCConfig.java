/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import net.minecraftforge.common.config.Configuration;

import java.util.*;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

/**
 * Created by lukas on 31.07.14.
 */
public class RCConfig
{
    public static final String CATEGORY_VISUAL = "visual";
    public static final String CATEGORY_BALANCING = "balancing";

    public static boolean hideRedundantNegativeSpace;

    public static float minDistToSpawnForGeneration;
    public static float structureSpawnChanceModifier = 1.0f;
    private static Set<String> disabledStructures = new HashSet<>();
    private static Set<String> disabledModGeneration = new HashSet<>();
    private static Set<String> persistentDisabledStructures = new HashSet<>();
    private static Set<String> forceEnabledStructures = new HashSet<>();

    public static String spawnStructure;
    public static int spawnStructureShiftX, spawnStructureShiftZ;

    public static String commandPrefix;

    public static boolean savePlayerCache;

    public static boolean notifyAdminOnBlockCommands;

    public static void loadConfig(String configID)
    {
        if (configID == null || configID.equals(CATEGORY_GENERAL))
        {
            commandPrefix = RecurrentComplex.config.getString("commandPrefix", CATEGORY_GENERAL, "#", "The String that will be prefixed to every command, e.g. '#' -> '/#gen', '#paste' etc.");

            savePlayerCache = RecurrentComplex.config.getBoolean("savePlayerCache", CATEGORY_GENERAL, true, "Whether player caches like the clipboard and previewed operations will be saved and loaded.");

            notifyAdminOnBlockCommands = RecurrentComplex.config.getBoolean("notifyAdminOnBlockCommands", CATEGORY_GENERAL, false, "Disabling this will prevent spawn command blocks from notifying the server admins, as normal commands would.");
        }
        
        if (configID == null || configID.equals(CATEGORY_BALANCING))
        {
            minDistToSpawnForGeneration = RecurrentComplex.config.getFloat("minDistToSpawnForGeneration", CATEGORY_BALANCING, 30.0f, 0.0f, 500.0f, "Within this block radius, default structures won't spawn (in the main dimension).");
            structureSpawnChanceModifier = RecurrentComplex.config.getFloat("structureSpawnChance", CATEGORY_BALANCING, 1.0f, 0.0f, 10.0f, "How often do structures spawn?");

            disabledStructures.clear();
            disabledStructures.addAll(Arrays.asList(RecurrentComplex.config.getStringList("disabledStructures", CATEGORY_BALANCING, new String[0], "Structures that will be hindered from generating.")));
            disabledModGeneration.clear();
            disabledModGeneration.addAll(Arrays.asList(RecurrentComplex.config.getStringList("disabledModGeneration", CATEGORY_BALANCING, new String[0], "Structures from mods in this list will automatically be set not to generate.")));
            forceEnabledStructures.clear();
            forceEnabledStructures.addAll(Arrays.asList(RecurrentComplex.config.getStringList("forceEnabledStructures", CATEGORY_BALANCING, new String[0], "Structures that be set to generate (if in the right directory), no matter what")));

            spawnStructure = RecurrentComplex.config.getString("spawnStructure", CATEGORY_BALANCING, "", "The structure that will generate around the spawn point. It will start off in the center of the spawn area, and may need to be moved using the spawn shift.");
            spawnStructureShiftX = RecurrentComplex.config.getInt("spawnStructureShiftX", CATEGORY_BALANCING, 0, -100, 100, "The amount of blocks the spawn structure will be moved along the x axis on generation.");
            spawnStructureShiftZ = RecurrentComplex.config.getInt("spawnStructureShiftZ", CATEGORY_BALANCING, 0, -100, 100, "The amount of blocks the spawn structure will be moved along the z axis on generation.");
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

    public static boolean isModGenerationDisabled(String modID)
    {
        return disabledModGeneration.contains(modID);
    }
}
