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
    public static final String CATEGORY_CONTROLS = "controls";

    public static boolean hideRedundantNegativeSpace;

    public static float minDistToSpawnForGeneration;
    public static float structureSpawnChanceModifier = 1.0f;
    private static Set<String> disabledStructures = new HashSet<>();
    private static Set<String> disabledModGeneration = new HashSet<>();
    private static Set<String> persistentDisabledStructures = new HashSet<>();
    private static Set<String> forceEnabledStructures = new HashSet<>();

    private static Set<String> disabledBiomes = new HashSet<>();

    public static boolean avoidOverlappingGeneration;

    public static String commandPrefix;

    public static boolean savePlayerCache;

    public static boolean notifyAdminOnBlockCommands;

    public static int blockSelectorModifierKeys[];

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
            avoidOverlappingGeneration = RecurrentComplex.config.getBoolean("avoidOverlappingGeneration", CATEGORY_BALANCING, true, "Enabling this will cancel any structure generation if another structure is present at the cooridnate already.");

            minDistToSpawnForGeneration = RecurrentComplex.config.getFloat("minDistToSpawnForGeneration", CATEGORY_BALANCING, 30.0f, 0.0f, 500.0f, "Within this block radius, default structures won't spawn (in the main dimension).");
            structureSpawnChanceModifier = RecurrentComplex.config.getFloat("structureSpawnChance", CATEGORY_BALANCING, 1.0f, 0.0f, 10.0f, "How often do structures spawn?");

            disabledStructures.clear();
            disabledStructures.addAll(Arrays.asList(RecurrentComplex.config.getStringList("disabledStructures", CATEGORY_BALANCING, new String[0], "Structures that will be hindered from generating.")));
            disabledModGeneration.clear();
            disabledModGeneration.addAll(Arrays.asList(RecurrentComplex.config.getStringList("disabledModGeneration", CATEGORY_BALANCING, new String[0], "Structures from mods in this list will automatically be set not to generate.")));
            forceEnabledStructures.clear();
            forceEnabledStructures.addAll(Arrays.asList(RecurrentComplex.config.getStringList("forceEnabledStructures", CATEGORY_BALANCING, new String[0], "Structures that be set to generate (if in the right directory), no matter what")));

            disabledBiomes.clear();
            disabledBiomes.addAll(Arrays.asList(RecurrentComplex.config.getStringList("disabledBiomes", CATEGORY_BALANCING, new String[0], "Biomes in which no biomes will spawn at all. This is for biomes that generally don't work well with structures.")));
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

    public static boolean isGenerationEnabled(String biomeID)
    {
        return disabledBiomes.contains(biomeID);
    }
}
