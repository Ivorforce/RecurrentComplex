/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex;

import com.google.common.primitives.Floats;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import it.unimi.dsi.fastutil.Hash;
import ivorius.reccomplex.files.loading.RCFileSuffix;
import ivorius.reccomplex.utils.RawResourceLocation;
import ivorius.reccomplex.utils.algebra.ExpressionCache;
import ivorius.reccomplex.utils.expression.BiomeExpression;
import ivorius.reccomplex.utils.expression.CommandExpression;
import ivorius.reccomplex.utils.expression.DimensionExpression;
import ivorius.reccomplex.utils.expression.ResourceExpression;
import ivorius.reccomplex.world.gen.feature.decoration.RCBiomeDecorator;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.StructureSaveHandler;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.TransformerMulti;
import ivorius.reccomplex.world.storage.loot.GenericItemCollectionRegistry;
import net.minecraft.command.ICommandSender;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
    private static final TObjectFloatMap<String> spawnTweaks = new TObjectFloatHashMap<>(Hash.DEFAULT_INITIAL_SIZE, Hash.DEFAULT_LOAD_FACTOR, 1);
    public static float structureSpawnChanceModifier = 1.0f;
    public static boolean avoidOverlappingGeneration;
    public static boolean honorStructureGenerationOption;

    public static boolean generateNature;
    public static boolean decorationHacks;
    public static final TObjectDoubleMap<RCBiomeDecorator.DecorationType> baseDecorationWeights = new TObjectDoubleHashMap<>();

    public static int baseVillageSpawnWeight;
    public static double saplingTriggerChance;
    public static double baseSaplingSpawnWeight;

    public static String commandPrefix;
    private static final Map<String, CommandExpression> commandMatchers = new HashMap<>();
    public static int asCommandPermissionLevel;

    public static boolean savePlayerCache;
    public static boolean notifyAdminOnBlockCommands;

    public static boolean postWorldStatus;

    public static boolean memorizeDecoration;
    public static boolean memorizeSaplings;

    public static int[] blockSelectorModifierKeys;

    private static boolean lightweightMode;

    private static ResourceExpression structureLoadMatcher = new ResourceExpression(StructureRegistry.INSTANCE::has);
    private static ResourceExpression structureGenerationMatcher = new ResourceExpression(StructureRegistry.INSTANCE::has);

    private static ResourceExpression inventoryGeneratorLoadMatcher = new ResourceExpression(GenericItemCollectionRegistry.INSTANCE::has);
    private static ResourceExpression inventoryGeneratorGenerationMatcher = new ResourceExpression(GenericItemCollectionRegistry.INSTANCE::has);

    private static BiomeExpression universalBiomeExpression = new BiomeExpression();
    private static DimensionExpression universalDimensionExpression = new DimensionExpression();

    private static ResourceExpression failingStructureLogExpression = new ResourceExpression(s -> true);

    private static final List<String> universalTransformerPresets = new ArrayList<>();
    private static TransformerMulti universalTransformer;

    public static float mazePlacementReversesPerRoom;
    public static long mazeTimeout;

    public static final Map<String, Boolean> globalToggles = new HashMap<>();

    public static void loadConfig(String configID)
    {
        Configuration config = RecurrentComplex.config;

        if (configID == null || configID.equals(CATEGORY_GENERAL))
        {
            lightweightMode = config.getBoolean("lightweightMode", CATEGORY_GENERAL, false, "Enabling this will make the mod register as little as possible, which enables it to be used server-side only. Note that this prevents you from editing or importing structures.");

            commandPrefix = config.getString("commandPrefix", CATEGORY_GENERAL, "#", "The String that will be prefixed to every command, e.g. '#' -> '/#gen', '#paste' etc.");

            commandMatchers.clear();
            parseMap(config.getStringList("commandMatchers", CATEGORY_GENERAL, new String[0], "List of Command Expressions determining if a command can be executed. Example: #export:#3 | $Ivorforce"),
                    null, Function.identity(), "command matcher", (expression) -> ExpressionCache.of(new CommandExpression(), expression), commandMatchers::put);

            asCommandPermissionLevel = config.getInt("asCommandPermissionLevel", CATEGORY_DECORATION, 4, -1, 10, "The required permission level for /#as to function. Set to 2 for command blocks and OPs, 4 for only server, or -1 to disable. Note that this could be a security problem on low levels.");

            savePlayerCache = config.getBoolean("savePlayerCache", CATEGORY_GENERAL, true, "Whether player caches like the clipboard and previewed operations will be saved and loaded.");
            notifyAdminOnBlockCommands = config.getBoolean("notifyAdminOnBlockCommands", CATEGORY_GENERAL, false, "Disabling this will prevent spawn command blocks from notifying the server admins, as normal commands would.");

            postWorldStatus = config.getBoolean("postWorldStatus", CATEGORY_GENERAL, true, "Once per world, post the status of ReC to the admins.");

            memorizeDecoration = config.getBoolean("memorizeDecoration", CATEGORY_GENERAL, false, "Memorize decoration spawns like trees or mushrooms (for /#whatisthis). Since decoration is so common, it is recommended to use this only for debugging / balancing purposes.");
            memorizeSaplings = config.getBoolean("memorizeSaplings", CATEGORY_GENERAL, false, "Memorize sapling spawns (for /#whatisthis). Since saplings are so common, it is recommended to use this only for debugging / balancing purposes.");
        }

        if (configID == null || configID.equals(CATEGORY_BALANCING))
        {
            avoidOverlappingGeneration = config.getBoolean("avoidOverlappingGeneration", CATEGORY_BALANCING, true, "Enabling this will cancel any structure generation if another structure is present at the cooridnate already.");
            honorStructureGenerationOption = config.getBoolean("honorStructureGenerationOption", CATEGORY_BALANCING, true, "If disabled, Recurrent Complex will generate structures in worlds without the structure generation option.");

            generateNature = config.getBoolean("generateNature", CATEGORY_BALANCING, true, "Whether the nature (e.g. trees, mushrooms) added by the mod should be actively generating.");
            decorationHacks = config.getBoolean("decorationHacks", CATEGORY_BALANCING, true, "Enable hacks for a few decoration types. Disabling this may fix problems but will deactivate ReC overriding those types of structures.");

            minDistToSpawnForGeneration = config.getFloat("minDistToSpawnForGeneration", CATEGORY_BALANCING, 30.0f, 0.0f, 500.0f, "Within this block radius, default structures won't spawn (in the main dimension).");
            structureSpawnChanceModifier = config.getFloat("structureSpawnChance", CATEGORY_BALANCING, 1.0f, 0.0f, 10.0f, "How often do structures spawn?");

            spawnTweaks.clear();
            parseMap(config.getStringList("spawnTweaks", CATEGORY_GENERAL, new String[0], "List of spawn chance tweaks to structures: IceThorn:0.5"),
                    null, Function.identity(), "spawn tweak float", Floats::tryParse, spawnTweaks::put);

            structureLoadMatcher.setExpression(config.getString("structureLoadMatcher", CATEGORY_BALANCING, "", "Resource Expression that will be applied to each loading structure, determining if it should be loaded."));
            logExpressionException(structureLoadMatcher, "structureLoadMatcher", RecurrentComplex.logger);
            structureGenerationMatcher.setExpression(config.getString("structureGenerationMatcher", CATEGORY_BALANCING, "", "Resource Expression that will be applied to each loading structure, determining if it should be set to 'active'."));
            logExpressionException(structureGenerationMatcher, "structureGenerationMatcher", RecurrentComplex.logger);

            inventoryGeneratorLoadMatcher.setExpression(config.getString("inventoryGeneratorLoadMatcher", CATEGORY_BALANCING, "", "Resource Expression that will be applied to each loading inventory generator, determining if it should be loaded."));
            logExpressionException(inventoryGeneratorLoadMatcher, "inventoryGeneratorLoadMatcher", RecurrentComplex.logger);
            inventoryGeneratorGenerationMatcher.setExpression(config.getString("inventoryGeneratorGenerationMatcher", CATEGORY_BALANCING, "", "Resource Expression that will be applied to each loading inventory generator, determining if it should be set to 'active'."));
            logExpressionException(inventoryGeneratorGenerationMatcher, "inventoryGeneratorGenerationMatcher", RecurrentComplex.logger);

            universalBiomeExpression.setExpression(config.getString("universalBiomeMatcher", CATEGORY_BALANCING, "", "Biome Expression that will be checked for every single structure. Use this if you want to blacklist / whitelist specific biomes that shouldn't have structures."));
            logExpressionException(universalBiomeExpression, "universalBiomeMatcher", RecurrentComplex.logger);

            universalDimensionExpression.setExpression(config.getString("universalDimensionMatcher", CATEGORY_BALANCING, "", "Dimension Expression that will be checked for every single structure. Use this if you want to blacklist / whitelist specific dimensions that shouldn't have structures."));
            logExpressionException(universalDimensionExpression, "universalDimensionMatcher", RecurrentComplex.logger);

            failingStructureLogExpression.setExpression(config.getString("failingStructureLogExpression", CATEGORY_BALANCING, "", "Resource Expression that will restrict logging of structures that fail to generate."));
            logExpressionException(failingStructureLogExpression, "failingStructureLogExpression", RecurrentComplex.logger);

            customArtifactTag = Pair.of(
                    config.getString("customArtifactTag", CATEGORY_BALANCING, "", "Custom Inventory Generator to override when an artifact generation tag fires."),
                    config.getFloat("customArtifactChance", CATEGORY_BALANCING, 0.0f, 0, 1, "Chance to use the customArtifactTag when an artifact generation tag fires.")
            );
            customBookTag = Pair.of(
                    config.getString("customBookTag", CATEGORY_BALANCING, "", "Custom Inventory Generator to override when a book generation tag fires."),
                    config.getFloat("customBookChance", CATEGORY_BALANCING, 0.0f, 0, 1, "Chance to use the customArtifactTag when a book generation tag fires.")
            );

            mazePlacementReversesPerRoom = config.getFloat("mazePlacementReversesPerRoom", CATEGORY_BALANCING, 3, -1, 100, "Maximum number of reverses per room the maze generator can do. A higher number results in a better generation success rate, but may freeze the server temporarily.");
            mazeTimeout = config.getInt("mazeTimeout", CATEGORY_BALANCING, 20000, -1, 600000, "Maze generation timeout, in milliseconds. After the time is over, the maze generation will just give up.");

            universalTransformer = null;
            Collections.addAll(universalTransformerPresets, config.getStringList("universalTransformerPresets", CATEGORY_BALANCING, new String[0], "Transformer preset names that are gonna be applied to every single generating structure. Use this if you need to enforce specific rules (e.g. \"don't ever spawn wood blocks\" (with a replace transformer)."));

            globalToggles.clear();
            parseMap(config.getStringList("globalToggles", CATEGORY_BALANCING, new String[]{"treeLeavesDecay: true"}, "Global toggles that can be used in expressions. You can also add your own. Ex: 'treeLeavesDecay: true'."),
                    null, Function.identity(), "global toggle boolean", Boolean::valueOf, globalToggles::put);
        }
        if (configID == null || configID.equals(CATEGORY_DECORATION))
        {
            baseVillageSpawnWeight = config.getInt("baseVillageSpawnWeight", CATEGORY_DECORATION, 10, 0, 100000, "The base weight of RC village generation types. Vanilla average is about 10 - if you want to fully replace vanilla structures in villages, crank this up to something big.");
            saplingTriggerChance = config.getFloat("saplingTriggerChance", CATEGORY_DECORATION, 1f, 0, 1, "The chance to trigger any special sapling spawns at all. If you want to disable the big trees, set this to 0.");
            baseSaplingSpawnWeight = config.getFloat("baseSaplingSpawnWeight", CATEGORY_DECORATION, 0.2f, 0, 100000, "The base weight of RC sapling generation types. The vanilla tree weight is 1 - if you want to fully replace vanilla trees, crank this up to something big.");

            baseDecorationWeights.clear();
            for (RCBiomeDecorator.DecorationType decorationType : RCBiomeDecorator.DecorationType.values())
                baseDecorationWeights.put(decorationType, config.getFloat("baseWeight_" + decorationType.id(), CATEGORY_DECORATION, 0.2f, 0, 1000, "The base weight of this decoration type. The vanilla decorator has a weight of 1 - if you want to fully replace vanilla decoration, crank this up to something big."));
        }

        RecurrentComplex.proxy.loadConfig(configID);
    }

    private static <K, V> void parseMap(String[] strings, String keyName, Function<String, K> keyFunc, String valueName, Function<String, V> valueFunc, BiConsumer<K, V> map)
    {
        for (String string : strings)
            parseMap(string, (ks, vs) ->
            {
                K key = keyFunc.apply(ks.trim());
                if (key == null)
                {
                    RecurrentComplex.logger.error("Failed parsing " + keyName + " key ''" + ks + "' for configuration");
                    return;
                }

                V val = valueFunc.apply(vs.trim());
                if (val == null)
                {
                    RecurrentComplex.logger.error("Failed parsing " + valueName + " value ''" + vs + "' for configuration");
                    return;
                }

                map.accept(key, val);
            });
    }

    private static void parseMap(String[] strings, BiConsumer<String, String> consumer)
    {
        for (String string : strings)
            parseMap(string, consumer);
    }

    private static void parseMap(String string, BiConsumer<String, String> consumer)
    {
        String[] parts = string.split(":", 2);
        if (parts.length == 2)
            consumer.accept(parts[0], parts[1]);
        else
            RecurrentComplex.logger.error("Failed finding key (separated by ':') in ''" + string + "'");
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

    public static boolean shouldResourceLoad(String fileSuffix, String id, String domain)
    {
        if (fileSuffix.equals(StructureSaveHandler.INSTANCE.suffix))
            return structureLoadMatcher.test(new RawResourceLocation(domain, id));
        else if (fileSuffix.equals(RCFileSuffix.INVENTORY_GENERATION_COMPONENT))
            return inventoryGeneratorLoadMatcher.test(new RawResourceLocation(domain, id));

        return true;
    }

    public static boolean shouldStructureGenerate(String id, String domain)
    {
        return structureGenerationMatcher.test(new RawResourceLocation(domain, id));
    }

    public static boolean shouldInventoryGeneratorGenerate(String id, String domain)
    {
        return inventoryGeneratorGenerationMatcher.test(new RawResourceLocation(domain, id));
    }

    public static float tweakedSpawnRate(String structure)
    {
        return structure != null ? spawnTweaks.get(structure) : 1;
    }

    public static boolean isGenerationEnabled(Biome biome)
    {
        return !universalBiomeExpression.isExpressionValid() || universalBiomeExpression.test(biome);
    }

    public static boolean isGenerationEnabled(WorldProvider provider)
    {
        return !universalDimensionExpression.isExpressionValid() || universalDimensionExpression.test(provider);
    }

    public static boolean canUseCommand(String command, ICommandSender sender)
    {
        CommandExpression matcher = commandMatchers.get(command);
        return matcher == null || matcher.test(new CommandExpression.Argument(command, sender));
    }

    public static boolean logFailingStructure(Structure structure)
    {
        RawResourceLocation loc = StructureRegistry.INSTANCE.resourceLocation(structure);
        return loc == null || failingStructureLogExpression.test(loc);
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
