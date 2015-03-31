/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.generic.matchers.BiomeMatcher;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;
import ivorius.ivtoolkit.random.WeightedSelector;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Created by lukas on 24.05.14.
 */
public class StructureSelector
{
    public static final int STRUCTURE_MIN_CAP_DEFAULT = 20;

    private static Map<String, Category> categories = new HashMap<>();

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

    public static void registerCategory(String id, Category category)
    {
        categories.put(id, category);
    }

    public static Category categoryForID(String id)
    {
        return categories.get(id);
    }

    public static Set<String> allCategoryIDs()
    {
        return categories.keySet();
    }

    public boolean isValid(BiomeGenBase biome, WorldProvider provider)
    {
        return DimensionDictionary.getDimensionTypes(provider).equals(cachedDimensionTypes);
    }

    public float generationChance(String category, BiomeGenBase biome)
    {
        Category categoryObj = categoryForID(category);

        if (categoryObj != null)
            return categoryObj.structureSpawnChance(biome, weightedStructureInfos.get(category).size()) * RCConfig.structureSpawnChanceModifier;

        return 0.0f;
    }

    public List<Pair<StructureInfo, NaturalGenerationInfo>> generatedStructures(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
    {
        List<Pair<StructureInfo, NaturalGenerationInfo>> infos = new ArrayList<>();
        BiomeGenBase biome = world.getBiomeGenForCoords(chunkX * 16, chunkZ * 16);

        for (String category : weightedStructureInfos.keySet())
        {
            if (random.nextFloat() < generationChance(category, biome))
                infos.add(WeightedSelector.select(random, weightedStructureInfos.get(category)));
        }

        return infos;
    }

    public static interface Category
    {
        float structureSpawnChance(BiomeGenBase biome, int registeredStructures);

        boolean selectableInGUI();
    }

    public static class SimpleCategory implements Category
    {
        public float defaultSpawnChance;
        public List<GenerationInfo> generationInfos;
        public boolean selectableInGUI;
        public int structureMinCap;

        public SimpleCategory(float defaultSpawnChance, List<GenerationInfo> generationInfos, boolean selectableInGUI, int structureMinCap)
        {
            this.defaultSpawnChance = defaultSpawnChance;
            this.generationInfos = generationInfos;
            this.selectableInGUI = selectableInGUI;
            this.structureMinCap = structureMinCap;
        }

        public SimpleCategory(float defaultSpawnChance, List<GenerationInfo> generationInfos, boolean selectableInGUI)
        {
            this(defaultSpawnChance, generationInfos, selectableInGUI, STRUCTURE_MIN_CAP_DEFAULT);
        }

        @Override
        public float structureSpawnChance(BiomeGenBase biome, int registeredStructures)
        {
            float amountMultiplier = Math.min((float) registeredStructures / (float) structureMinCap, 1.0f);

            for (GenerationInfo info : generationInfos)
            {
                if (info.selector.apply(biome))
                    return info.spawnChance * amountMultiplier;
            }

            return defaultSpawnChance * amountMultiplier;
        }

        @Override
        public boolean selectableInGUI()
        {
            return selectableInGUI;
        }
    }

    public static class GenerationInfo
    {
        public float spawnChance;
        public BiomeMatcher selector;

        public GenerationInfo(float spawnChance, BiomeMatcher selector)
        {
            this.spawnChance = spawnChance;
            this.selector = selector;
        }
    }
}
