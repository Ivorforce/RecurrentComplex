/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.worldgen.selector;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.generic.gentypes.EnvironmentalSelection;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 24.05.14.
 */
public class StructureSelector<T extends StructureGenerationInfo & EnvironmentalSelection<C>, C extends StructureSelector.Category>
{
    private final Set<String> cachedDimensionTypes = new HashSet<>(); // Because dimensions could often change on the fly

    private Multimap<C, WeightedSelector.SimpleItem<Pair<StructureInfo, T>>> weightedStructureInfos = ArrayListMultimap.create();

    public StructureSelector(Collection<StructureInfo> structures, Biome biome, WorldProvider provider, Class<T> typeClass)
    {
        cachedDimensionTypes.addAll(DimensionDictionary.getDimensionTypes(provider));

        for (StructureInfo<?> structureInfo : structures)
        {
            List<T> generationInfos = structureInfo.generationInfos(typeClass);
            for (T selection : generationInfos)
            {
                double generationWeight = selection.getGenerationWeight(biome, provider);

                if (generationWeight > 0)
                    weightedStructureInfos.put(selection.generationCategory(), new WeightedSelector.SimpleItem<>(generationWeight, Pair.of(structureInfo, selection)));
            }
        }
    }

    public boolean isValid(Biome biome, WorldProvider provider)
    {
        return DimensionDictionary.getDimensionTypes(provider).equals(cachedDimensionTypes);
    }

    public float generationChance(C category, Biome biome, WorldProvider worldProvider)
    {
        if (category != null)
            return category.structureSpawnChance(biome, worldProvider, weightedStructureInfos.get(category).size());

        return 0.0f;
    }

    public List<Pair<StructureInfo, T>> generatedStructures(Random random, ChunkPos chunkPos, World world)
    {
        Biome biome = world.getBiome(chunkPos.getBlock(0, 0, 0));

        return weightedStructureInfos.keySet().stream()
                .filter(category -> random.nextFloat() < generationChance(category, biome, world.provider))
                .map(category -> WeightedSelector.select(random, weightedStructureInfos.get(category)))
                .collect(Collectors.toList());
    }

    @Nullable
    public Pair<StructureInfo, T> selectOne(Random random, ChunkPos chunkPos, World world)
    {
        Biome biome = world.getBiome(chunkPos.getBlock(0, 0, 0));

        List<WeightedSelector.SimpleItem<C>> list = weightedStructureInfos.keySet().stream()
                .map(category -> new WeightedSelector.SimpleItem<>(generationChance(category, biome, world.provider), category)).collect(Collectors.toList());

        if (WeightedSelector.canSelect(list))
            return WeightedSelector.select(random, weightedStructureInfos.get(WeightedSelector.select(random, list)));
        else
            return null;
    }

    interface Category
    {
        float structureSpawnChance(Biome biome, WorldProvider worldProvider, int registeredStructures);
    }
}
