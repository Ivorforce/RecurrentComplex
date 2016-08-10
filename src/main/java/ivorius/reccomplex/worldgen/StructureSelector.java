/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.annotations.SerializedName;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.generic.matchers.BiomeMatcher;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.reccomplex.structures.generic.matchers.DimensionMatcher;
import ivorius.reccomplex.utils.CustomizableMap;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 24.05.14.
 */
public class StructureSelector
{
    public static final int STRUCTURE_MIN_CAP_DEFAULT = 20;

    private static CustomizableMap<String, Category> categories = new CustomizableMap<>();

    private Multimap<String, WeightedSelector.SimpleItem<Pair<StructureInfo, NaturalGenerationInfo>>> weightedStructureInfos = ArrayListMultimap.create();

    private final Set<String> cachedDimensionTypes = new HashSet<>();

    public StructureSelector(Collection<StructureInfo> structures, BiomeGenBase biome, WorldProvider provider)
    {
        cachedDimensionTypes.addAll(DimensionDictionary.getDimensionTypes(provider));

        for (StructureInfo structureInfo : structures)
        {
            List<NaturalGenerationInfo> generationInfos = structureInfo.generationInfos(NaturalGenerationInfo.class);
            for (NaturalGenerationInfo naturalGenerationInfo : generationInfos)
            {
                double generationWeight = naturalGenerationInfo.getGenerationWeight(biome, provider);

                if (generationWeight > 0)
                    weightedStructureInfos.put(naturalGenerationInfo.generationCategory, new WeightedSelector.SimpleItem<>(generationWeight, Pair.of(structureInfo, naturalGenerationInfo)));
            }
        }
    }

    public static void registerCategory(String id, Category category, boolean custom)
    {
        categories.put(id, category, custom);
    }

    public static void unregisterCategory(String id, boolean custom)
    {
        categories.remove(id, custom);
    }

    public static void clearCustom()
    {
        categories.clearCustom();
    }

    public static Category categoryForID(String id)
    {
        return categories.getMap().get(id);
    }

    public static Set<String> allCategoryIDs()
    {
        return categories.getMap().keySet();
    }

    public boolean isValid(BiomeGenBase biome, WorldProvider provider)
    {
        return DimensionDictionary.getDimensionTypes(provider).equals(cachedDimensionTypes);
    }

    public float generationChance(String category, BiomeGenBase biome, WorldProvider worldProvider)
    {
        Category categoryObj = categoryForID(category);

        if (categoryObj != null)
            return categoryObj.structureSpawnChance(biome, worldProvider, weightedStructureInfos.get(category).size()) * RCConfig.structureSpawnChanceModifier;

        return 0.0f;
    }

    public List<Pair<StructureInfo, NaturalGenerationInfo>> generatedStructures(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
    {
        BiomeGenBase biome = world.getBiomeGenForCoords(new BlockPos(chunkX * 16, 0, chunkZ * 16));

        return weightedStructureInfos.keySet().stream()
                .filter(category -> random.nextFloat() < generationChance(category, biome, world.provider))
                .map(category -> WeightedSelector.select(random, weightedStructureInfos.get(category)))
                .collect(Collectors.toList());
    }

    public Pair<StructureInfo, NaturalGenerationInfo> selectOne(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
    {
        BiomeGenBase biome = world.getBiomeGenForCoords(new BlockPos(chunkX * 16, 0, chunkZ * 16));

        List<WeightedSelector.SimpleItem<String>> list = weightedStructureInfos.keySet().stream()
                .map(category -> new WeightedSelector.SimpleItem<>(generationChance(category, biome, world.provider), category)).collect(Collectors.toList());

        return WeightedSelector.select(random, weightedStructureInfos.get(WeightedSelector.select(random, list)));
    }

    public interface Category
    {
        float structureSpawnChance(BiomeGenBase biome, WorldProvider worldProvider, int registeredStructures);

        boolean selectableInGUI();

        String title();

        List<String> tooltip();
    }

    public static class SimpleCategory implements Category
    {
        @SerializedName("defaultSpawnChance")
        public float defaultSpawnChance;

        @SerializedName("generationInfos")
        public final List<GenerationInfo> generationInfos = new ArrayList<>();

        @SerializedName("selectableInGUI")
        public boolean selectableInGUI;

        @SerializedName("structureMinCap")
        public Integer structureMinCap;

        @SerializedName("title")
        public String title;

        @SerializedName("tooltip")
        public final List<String> tooltip = new ArrayList<>();

        public SimpleCategory(float defaultSpawnChance, List<GenerationInfo> generationInfos, boolean selectableInGUI, Integer structureMinCap)
        {
            this.defaultSpawnChance = defaultSpawnChance;
            this.generationInfos.addAll(generationInfos);
            this.selectableInGUI = selectableInGUI;
            this.structureMinCap = structureMinCap;
        }

        public SimpleCategory(float defaultSpawnChance, List<GenerationInfo> generationInfos, boolean selectableInGUI)
        {
            this(defaultSpawnChance, generationInfos, selectableInGUI, null);
        }

        @Override
        public float structureSpawnChance(BiomeGenBase biome, WorldProvider worldProvider, int registeredStructures)
        {
            float amountMultiplier = Math.min((float) registeredStructures / (float) getActiveStructureMinCap(), 1.0f);

            for (GenerationInfo info : generationInfos)
            {
                if (info.biomeMatcher.apply(biome) && info.dimensionMatcher.apply(worldProvider))
                    return info.spawnChance * amountMultiplier;
            }

            return defaultSpawnChance * amountMultiplier;
        }

        public Integer getActiveStructureMinCap()
        {
            return structureMinCap != null ? structureMinCap : STRUCTURE_MIN_CAP_DEFAULT;
        }

        @Override
        public boolean selectableInGUI()
        {
            return selectableInGUI;
        }

        @Override
        public String title()
        {
            return title;
        }

        @Override
        public List<String> tooltip()
        {
            return tooltip;
        }
    }

    public static class GenerationInfo
    {
        @SerializedName("spawnChance")
        public float spawnChance;
        @SerializedName("biomeMatcher")
        public BiomeMatcher biomeMatcher;
        @SerializedName("dimensionMatcher")
        public DimensionMatcher dimensionMatcher;

        public GenerationInfo(float spawnChance, BiomeMatcher biomeMatcher, DimensionMatcher dimensionMatcher)
        {
            this.spawnChance = spawnChance;
            this.biomeMatcher = biomeMatcher;
            this.dimensionMatcher = dimensionMatcher;
        }
    }
}
