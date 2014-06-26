/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.worldgen;

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
    private Map<String, List<WeightedStructureInfo>> weightedStructureInfos = new HashMap<>();

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

    public static float generationChance(String category)
    {
        switch (category)
        {
            case "decoration":
                return 1.0f / 20.0f;
            case "adventure":
                return 1.0f / 200.0f;
            case "rare":
                return 1.0f / 1000.0f;
        }

        return 0.01f;
    }

    public List<StructureInfo> generatedStructures(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
    {
        List<StructureInfo> infos = new ArrayList<>();

        for (String category : weightedStructureInfos.keySet())
        {
            if (random.nextFloat() < generationChance(category))
            {
                List<WeightedStructureInfo> structureInfos = weightedStructureInfos.get(category);

                WeightedStructureInfo structureInfo = (WeightedStructureInfo) WeightedRandom.getRandomItem(random, structureInfos);
                infos.add(structureInfo.structureInfo);
            }
        }

        return infos;
    }
}
