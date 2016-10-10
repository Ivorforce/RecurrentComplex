/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.selector;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.world.gen.feature.structure.StructureInfo;
import ivorius.reccomplex.world.gen.feature.structure.generic.BiomeGenerationInfo;
import ivorius.reccomplex.world.gen.feature.structure.generic.DimensionGenerationInfo;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.GenerationInfo;
import ivorius.reccomplex.utils.presets.PresettedList;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by lukas on 24.05.14.
 */
public class StructureSelector<T extends GenerationInfo & EnvironmentalSelection<C>, C>
{
    protected final Set<String> cachedDimensionTypes = new HashSet<>(); // Because dimensions could often change on the fly

    protected Multimap<C, WeightedSelector.SimpleItem<Pair<StructureInfo, T>>> weightedStructureInfos = ArrayListMultimap.create();
    protected TObjectDoubleMap<C> totalWeights = new TObjectDoubleHashMap<C>();

    public StructureSelector(Map<String, StructureInfo> structures, WorldProvider provider, Biome biome, Class<T> typeClass)
    {
        cachedDimensionTypes.addAll(DimensionDictionary.getDimensionTypes(provider));

        for (Map.Entry<String, StructureInfo> entry : structures.entrySet())
        {
            float tweaked = RCConfig.tweakedSpawnRate(entry.getKey());
            for (T selection : (List<T>) entry.getValue().generationInfos(typeClass))
            {
                double generationWeight = selection.getGenerationWeight(provider, biome) * tweaked;

                if (generationWeight > 0)
                {
                    weightedStructureInfos.put(selection.generationCategory(), new WeightedSelector.SimpleItem<>(generationWeight, Pair.of(entry.getValue(), selection)));
                    totalWeights.adjustValue(selection.generationCategory(), generationWeight);
                }
            }
        }
    }

    public static double generationWeight(WorldProvider provider, Biome biome, PresettedList<BiomeGenerationInfo> biomeWeights, PresettedList<DimensionGenerationInfo> dimensionWeights)
    {
        return generationWeightInBiome(biomeWeights, biome)
                * generationWeightInDimension(dimensionWeights, provider);
    }

    public static double generationWeightInDimension(PresettedList<DimensionGenerationInfo> dimensionWeights, WorldProvider provider)
    {
        for (DimensionGenerationInfo generationInfo : dimensionWeights.getContents())
        {
            if (generationInfo.matches(provider))
                return generationInfo.getActiveGenerationWeight();
        }

        return 0;
    }

    public static double generationWeightInBiome(PresettedList<BiomeGenerationInfo> biomeWeights, Biome biome)
    {
        for (BiomeGenerationInfo generationInfo : biomeWeights.getContents())
        {
            if (generationInfo.matches(biome))
                return generationInfo.getActiveGenerationWeight();
        }

        return 0;
    }

    public double totalWeight(@Nonnull C c)
    {
        return totalWeights.get(c);
    }

    @Nullable
    public Pair<StructureInfo, T> selectOne(Random random, @Nonnull C c)
    {
        return selectOne(random, c, totalWeight(c));
    }

    @Nullable
    public Pair<StructureInfo, T> selectOne(Random random, @Nonnull C c, double totalWeight)
    {
        return totalWeight > 0 ? WeightedSelector.select(random, weightedStructureInfos.get(c)) : null;
    }

    public boolean isValid(Biome biome, WorldProvider provider)
    {
        return DimensionDictionary.getDimensionTypes(provider).equals(cachedDimensionTypes);
    }
}
