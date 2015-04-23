/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import ivorius.reccomplex.structures.generic.matchers.BiomeMatcher;
import ivorius.reccomplex.structures.generic.matchers.DimensionMatcher;
import ivorius.reccomplex.utils.ExpressionCache;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
    public static boolean avoidOverlappingGeneration;

    public static int baseVillageSpawnWeight;

    public static String commandPrefix;

    public static boolean savePlayerCache;
    public static boolean notifyAdminOnBlockCommands;

    public static int[] blockSelectorModifierKeys;

    private static boolean lightweightMode;

    private static Set<String> disabledStructures = new HashSet<>();
    private static Set<String> disabledModGeneration = new HashSet<>();
    private static Set<String> persistentDisabledStructures = new HashSet<>();
    private static Set<String> forceEnabledStructures = new HashSet<>();

    private static BiomeMatcher universalBiomeMatcher = new BiomeMatcher("");
    private static DimensionMatcher universalDimensionMatcher = new DimensionMatcher("");

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
            lightweightMode = RecurrentComplex.config.getBoolean("lightweightMode", CATEGORY_BALANCING, false, "Enabling this will make the mod register as little as possible, which enables it to be used server-side only.");

            avoidOverlappingGeneration = RecurrentComplex.config.getBoolean("avoidOverlappingGeneration", CATEGORY_BALANCING, true, "Enabling this will cancel any structure generation if another structure is present at the cooridnate already.");
            baseVillageSpawnWeight = RecurrentComplex.config.getInt("baseVillageSpawnWeight", CATEGORY_BALANCING, 10, 0, 100000, "The base weight of RC village generation types. Vanilla average is about 10 - if you want to fully replace vanilla structures in villages, crank this up to something big.");

            minDistToSpawnForGeneration = RecurrentComplex.config.getFloat("minDistToSpawnForGeneration", CATEGORY_BALANCING, 30.0f, 0.0f, 500.0f, "Within this block radius, default structures won't spawn (in the main dimension).");
            structureSpawnChanceModifier = RecurrentComplex.config.getFloat("structureSpawnChance", CATEGORY_BALANCING, 1.0f, 0.0f, 10.0f, "How often do structures spawn?");

            disabledStructures.clear();
            disabledStructures.addAll(Arrays.asList(RecurrentComplex.config.getStringList("disabledStructures", CATEGORY_BALANCING, new String[0], "Structures that will be hindered from generating.")));
            disabledModGeneration.clear();
            disabledModGeneration.addAll(Arrays.asList(RecurrentComplex.config.getStringList("disabledModGeneration", CATEGORY_BALANCING, new String[0], "Structures from mods in this list will automatically be set not to generate.")));
            forceEnabledStructures.clear();
            forceEnabledStructures.addAll(Arrays.asList(RecurrentComplex.config.getStringList("forceEnabledStructures", CATEGORY_BALANCING, new String[0], "Structures that be set to generate (if in the right directory), no matter what")));

            universalBiomeMatcher.setExpression(RecurrentComplex.config.getString("universalBiomeMatcher", CATEGORY_BALANCING, "", "Biome Expression that will be checked for every single structure. Use this if you want to blacklist / whitelist specific biomes that shouldn't have structures."));
            logExpressionException(universalBiomeMatcher, "universalBiomeMatcher", RecurrentComplex.logger);

            universalDimensionMatcher.setExpression(RecurrentComplex.config.getString("universalDimensionMatcher", CATEGORY_BALANCING, "", "Dimension Expression that will be checked for every single structure. Use this if you want to blacklist / whitelist specific dimensions that shouldn't have structures."));
            logExpressionException(universalDimensionMatcher, "universalDimensionMatcher", RecurrentComplex.logger);
        }

        RecurrentComplex.proxy.loadConfig(configID);
    }

    private static void logExpressionException(ExpressionCache<?> cache, String name, Logger logger)
    {
        if (cache.getParseException() != null)
            logger.error("Error in expression '" + name + "'", cache.getParseException());
    }

    public static boolean isLightweightMode()
    {
        return lightweightMode;
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

    public static boolean isGenerationEnabled(BiomeGenBase biome)
    {
        return universalBiomeMatcher.apply(biome);
    }

    public static boolean isGenerationEnabled(WorldProvider provider)
    {
        return universalDimensionMatcher.apply(provider);
    }
}
