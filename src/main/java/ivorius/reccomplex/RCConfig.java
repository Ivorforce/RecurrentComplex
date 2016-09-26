/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import com.google.common.collect.Lists;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.matchers.BiomeMatcher;
import ivorius.reccomplex.structures.generic.matchers.CommandMatcher;
import ivorius.reccomplex.structures.generic.matchers.DimensionMatcher;
import ivorius.reccomplex.structures.generic.matchers.ResourceMatcher;
import ivorius.reccomplex.structures.generic.transformers.TransformerMulti;
import ivorius.reccomplex.utils.ExpressionCache;
import ivorius.reccomplex.worldgen.decoration.RCBiomeDecorator;
import ivorius.reccomplex.worldgen.inventory.GenericItemCollectionRegistry;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Consumer;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

/**
 * Created by lukas on 31.07.14.
 */
public class RCConfig
{
    public static final String CATEGORY_VISUAL = "visual";
    public static final String CATEGORY_BALANCING = "balancing";
    public static final String CATEGORY_DECORATION = "decoration";
    public static final String CATEGORY_CONTROLS = "controls";

    public static Pair<String, Float> customArtifactTag = Pair.of("", 0.0f);
    public static Pair<String, Float> customBookTag = Pair.of("", 0.0f);

    public static boolean hideRedundantNegativeSpace;

    public static float minDistToSpawnForGeneration;
    public static float structureSpawnChanceModifier = 1.0f;
    public static boolean avoidOverlappingGeneration;
    public static boolean honorStructureGenerationOption;

    public static boolean generateNature;
    public static final TObjectDoubleMap<RCBiomeDecorator.DecorationType> baseDecorationWeights = new TObjectDoubleHashMap<>();

    public static int baseVillageSpawnWeight;
    public static double saplingTriggerChance;
    public static double baseSaplingSpawnWeight;

    public static String commandPrefix;
    private static final Map<String, CommandMatcher> commandMatchers = new HashMap<>();

    public static boolean savePlayerCache;
    public static boolean notifyAdminOnBlockCommands;

    public static boolean memorizeDecoration;
    public static boolean memorizeSaplings;

    public static int[] blockSelectorModifierKeys;

    private static boolean lightweightMode;

    private static ResourceMatcher structureLoadMatcher = new ResourceMatcher("", StructureRegistry.INSTANCE::hasStructure);
    private static ResourceMatcher structureGenerationMatcher = new ResourceMatcher("", StructureRegistry.INSTANCE::hasStructure);

    private static ResourceMatcher inventoryGeneratorLoadMatcher = new ResourceMatcher("", GenericItemCollectionRegistry.INSTANCE::isLoaded);
    private static ResourceMatcher inventoryGeneratorGenerationMatcher = new ResourceMatcher("", GenericItemCollectionRegistry.INSTANCE::isLoaded);

    private static BiomeMatcher universalBiomeMatcher = new BiomeMatcher("");
    private static DimensionMatcher universalDimensionMatcher = new DimensionMatcher("");

    private static final List<String> universalTransformerPresets = new ArrayList<>();
    private static TransformerMulti universalTransformer;

    public static float mazePlacementReversesPerRoom;

    public static void loadConfig(String configID)
    {
        Configuration config = RecurrentComplex.config;

        if (configID == null || configID.equals(CATEGORY_GENERAL))
        {
            commandPrefix = config.getString("commandPrefix", CATEGORY_GENERAL, "#", "The String that will be prefixed to every command, e.g. '#' -> '/#gen', '#paste' etc.");

            commandMatchers.clear();
            Lists.newArrayList(config.getStringList("commandMatchers", CATEGORY_GENERAL, new String[0], "List of Command Expressions determining if a command can be executed. Example: #export:#3 | $Ivorforce")).forEach(string ->
                    parseMap(string, parts ->
                    {
                        CommandMatcher value = new CommandMatcher(parts[1]);
                        if (value.getParseException() == null)
                            commandMatchers.put(parts[0], value);
                        else
                            RecurrentComplex.logger.error("Failed parsing command matcher ''" + parts[1] + "'");
                    })
            );

            savePlayerCache = config.getBoolean("savePlayerCache", CATEGORY_GENERAL, true, "Whether player caches like the clipboard and previewed operations will be saved and loaded.");

            notifyAdminOnBlockCommands = config.getBoolean("notifyAdminOnBlockCommands", CATEGORY_GENERAL, false, "Disabling this will prevent spawn command blocks from notifying the server admins, as normal commands would.");

            memorizeDecoration = config.getBoolean("memorizeDecoration", CATEGORY_GENERAL, false, "Memorize decoration spawns like trees or mushrooms (for /#whatisthis). Since decoration is so common, it is recommended to use this only for debugging / balancing purposes.");
            memorizeSaplings = config.getBoolean("memorizeSaplings", CATEGORY_GENERAL, false, "Memorize sapling spawns (for /#whatisthis). Since saplings are so common, it is recommended to use this only for debugging / balancing purposes.");
        }

        if (configID == null || configID.equals(CATEGORY_BALANCING))
        {
            lightweightMode = config.getBoolean("lightweightMode", CATEGORY_BALANCING, false, "Enabling this will make the mod register as little as possible, which enables it to be used server-side only.");

            avoidOverlappingGeneration = config.getBoolean("avoidOverlappingGeneration", CATEGORY_BALANCING, true, "Enabling this will cancel any structure generation if another structure is present at the cooridnate already.");
            honorStructureGenerationOption = config.getBoolean("honorStructureGenerationOption", CATEGORY_BALANCING, true, "If disabled, Recurrent Complex will generate structures in worlds without the structure generation option.");

            generateNature = config.getBoolean("generateNature", CATEGORY_BALANCING, true, "Whether the nature (e.g. trees, mushrooms) added by the mod should be actively generating.");

            minDistToSpawnForGeneration = config.getFloat("minDistToSpawnForGeneration", CATEGORY_BALANCING, 30.0f, 0.0f, 500.0f, "Within this block radius, default structures won't spawn (in the main dimension).");
            structureSpawnChanceModifier = config.getFloat("structureSpawnChance", CATEGORY_BALANCING, 1.0f, 0.0f, 10.0f, "How often do structures spawn?");

            structureLoadMatcher.setExpression(config.getString("structureLoadMatcher", CATEGORY_BALANCING, "", "Resource Expression that will be applied to each loading structure, determining if it should be loaded."));
            logExpressionException(structureLoadMatcher, "structureLoadMatcher", RecurrentComplex.logger);
            structureGenerationMatcher.setExpression(config.getString("structureGenerationMatcher", CATEGORY_BALANCING, "", "Resource Expression that will be applied to each loading structure, determining if it should be set to 'active'."));
            logExpressionException(structureGenerationMatcher, "structureGenerationMatcher", RecurrentComplex.logger);

            inventoryGeneratorLoadMatcher.setExpression(config.getString("inventoryGeneratorLoadMatcher", CATEGORY_BALANCING, "", "Resource Expression that will be applied to each loading inventory generator, determining if it should be loaded."));
            logExpressionException(inventoryGeneratorLoadMatcher, "inventoryGeneratorLoadMatcher", RecurrentComplex.logger);
            inventoryGeneratorGenerationMatcher.setExpression(config.getString("inventoryGeneratorGenerationMatcher", CATEGORY_BALANCING, "", "Resource Expression that will be applied to each loading inventory generator, determining if it should be set to 'active'."));
            logExpressionException(inventoryGeneratorGenerationMatcher, "inventoryGeneratorGenerationMatcher", RecurrentComplex.logger);

            universalBiomeMatcher.setExpression(config.getString("universalBiomeMatcher", CATEGORY_BALANCING, "", "Biome Expression that will be checked for every single structure. Use this if you want to blacklist / whitelist specific biomes that shouldn't have structures."));
            logExpressionException(universalBiomeMatcher, "universalBiomeMatcher", RecurrentComplex.logger);

            universalDimensionMatcher.setExpression(config.getString("universalDimensionMatcher", CATEGORY_BALANCING, "", "Dimension Expression that will be checked for every single structure. Use this if you want to blacklist / whitelist specific dimensions that shouldn't have structures."));
            logExpressionException(universalDimensionMatcher, "universalDimensionMatcher", RecurrentComplex.logger);

            customArtifactTag = Pair.of(
                    config.getString("customArtifactTag", CATEGORY_BALANCING, "", "Custom Inventory Generator to override when an artifact generation tag fires."),
                    config.getFloat("customArtifactChance", CATEGORY_BALANCING, 0.0f, 0, 1, "Chance to use the customArtifactTag when an artifact generation tag fires.")
            );
            customBookTag = Pair.of(
                    config.getString("customBookTag", CATEGORY_BALANCING, "", "Custom Inventory Generator to override when a book generation tag fires."),
                    config.getFloat("customBookChance", CATEGORY_BALANCING, 0.0f, 0, 1, "Chance to use the customArtifactTag when a book generation tag fires.")
            );

            mazePlacementReversesPerRoom = config.getFloat("mazePlacementReversesPerRoom", CATEGORY_BALANCING, 10, -1, 100, "Maximum number of reverses per room the maze generator can do. A higher number results in a better generation success rate, but may freeze the server temporarily.");

            universalTransformer = null;
            Collections.addAll(universalTransformerPresets, config.getStringList("universalTransformerPresets", CATEGORY_BALANCING, new String[0], "Transformer preset names that are gonna be applied to every single generating structure. Use this if you need to enforce specific rules (e.g. \"don't ever spawn wood blocks\" (with a replace transformer)."));
        }
        if (configID == null || configID.equals(CATEGORY_DECORATION))
        {
            baseVillageSpawnWeight = config.getInt("baseVillageSpawnWeight", CATEGORY_DECORATION, 10, 0, 100000, "The base weight of RC village generation types. Vanilla average is about 10 - if you want to fully replace vanilla structures in villages, crank this up to something big.");
            saplingTriggerChance = config.getFloat("saplingTriggerChance", CATEGORY_DECORATION, 1f, 0, 1, "The chance to trigger any special sapling spawns at all. If you want to disable the big trees, set this to 0.");
            baseSaplingSpawnWeight = config.getFloat("baseSaplingSpawnWeight", CATEGORY_DECORATION, 0.2f, 0, 1000, "The base weight of RC sapling generation types. The vanilla tree weight is 1 - if you want to fully replace vanilla trees, crank this up to something big.");

            baseDecorationWeights.clear();
            for (RCBiomeDecorator.DecorationType decorationType : RCBiomeDecorator.DecorationType.values())
                baseDecorationWeights.put(decorationType, config.getFloat("baseWeight_" + decorationType.id(), CATEGORY_DECORATION, 0.2f, 0, 1000, "The base weight of this decoration type. The vanilla decorator has a weight of 1 - if you want to fully replace vanilla decoration, crank this up to something big."));
        }

        RecurrentComplex.proxy.loadConfig(configID);
    }

    private static void parseMap(String string, Consumer<String[]> consumer)
    {
        String[] parts = string.split(":", 2);
        if (parts.length > 1)
            consumer.accept(parts);
        else
            RecurrentComplex.logger.error("Failed finding key in command matcher ''" + string + "'");
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

    public static boolean shouldStructureLoad(String id, String domain)
    {
        return structureLoadMatcher.test(new ResourceLocation(domain, id));
    }

    public static boolean shouldStructureGenerate(String id, String domain)
    {
        return structureGenerationMatcher.test(new ResourceLocation(domain, id));
    }

    public static boolean shouldInventoryGeneratorLoad(String id, String domain)
    {
        return inventoryGeneratorLoadMatcher.test(new ResourceLocation(domain, id));
    }

    public static boolean shouldInventoryGeneratorGenerate(String id, String domain)
    {
        return inventoryGeneratorGenerationMatcher.test(new ResourceLocation(domain, id));
    }

    public static boolean isGenerationEnabled(Biome biome)
    {
        return !universalBiomeMatcher.isExpressionValid() || universalBiomeMatcher.test(biome);
    }

    public static boolean isGenerationEnabled(WorldProvider provider)
    {
        return !universalDimensionMatcher.isExpressionValid() || universalDimensionMatcher.test(provider);
    }

    public static boolean canUseCommand(String command, ICommandSender sender)
    {
        CommandMatcher matcher = commandMatchers.get(command);
        return matcher == null || matcher.test(new CommandMatcher.Argument(command, sender));
    }

    public static TransformerMulti getUniversalTransformer()
    {
        if (universalTransformer == null)
        {
            universalTransformer = new TransformerMulti("rc_universal", "");
            universalTransformer.getData().setToCustom();
            universalTransformer.getTransformers().clear();
            universalTransformerPresets.stream().map(TransformerMulti::fromPreset).forEach(universalTransformer.getTransformers()::add);
        }

        return universalTransformer;
    }
}
