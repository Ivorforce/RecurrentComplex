/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.worldgen.genericStructures.BiomeSelector;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.*;

/**
 * Created by lukas on 24.05.14.
 */
public class StructureSelector
{
    public static final int STRUCTURE_MIN_CAP_DEFAULT = 10;

    private static Map<String, Category> categories = new HashMap<>();

    private Map<String, List<WeightedStructureInfo>> weightedStructureInfos = new HashMap<>();

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

    public StructureSelector(Collection<StructureInfo> structures, BiomeGenBase biome)
    {
        for (StructureInfo structureInfo : structures)
        {
            int generationWeight = structureInfo.generationWeightInBiome(biome);

            if (generationWeight > 0)
            {
                String category = structureInfo.generationCategory();
                if (!weightedStructureInfos.containsKey(category))
                {
                    weightedStructureInfos.put(category, new ArrayList<WeightedStructureInfo>());
                }

                weightedStructureInfos.get(category).add(new WeightedStructureInfo(generationWeight, structureInfo));
            }
        }
    }

    public float generationChance(String category, BiomeGenBase biome)
    {
        Category categoryObj = categoryForID(category);

        if (categoryObj != null)
            return categoryObj.structureSpawnChance(biome, weightedStructureInfos.get(category).size()) * RCConfig.structureSpawnChanceModifier;

        return 0.0f;
    }

    public List<StructureInfo> generatedStructures(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
    {
        List<StructureInfo> infos = new ArrayList<>();
        BiomeGenBase biome = world.getBiomeGenForCoords(chunkX * 16, chunkZ * 16);

        for (String category : weightedStructureInfos.keySet())
        {
            if (random.nextFloat() < generationChance(category, biome))
            {
                List<WeightedStructureInfo> structureInfos = weightedStructureInfos.get(category);

                WeightedStructureInfo structureInfo = (WeightedStructureInfo) WeightedRandom.getRandomItem(random, structureInfos);
                infos.add(structureInfo.structureInfo);
            }
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
                if (info.selector.matches(biome))
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
        public BiomeSelector selector;

        public GenerationInfo(float spawnChance, BiomeSelector selector)
        {
            this.spawnChance = spawnChance;
            this.selector = selector;
        }
    }
}
