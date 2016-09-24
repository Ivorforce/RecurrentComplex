/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.worldgen.selector;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.reccomplex.dimensions.DimensionDictionary;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.generic.BiomeGenerationInfo;
import ivorius.reccomplex.structures.generic.DimensionGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
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
public class StructureSelector<T extends StructureGenerationInfo & EnvironmentalSelection<C>, C>
{
    protected final Set<String> cachedDimensionTypes = new HashSet<>(); // Because dimensions could often change on the fly

    protected Multimap<C, WeightedSelector.SimpleItem<Pair<StructureInfo, T>>> weightedStructureInfos = ArrayListMultimap.create();

    public StructureSelector(Collection<StructureInfo> structures, WorldProvider provider, Biome biome, Class<T> typeClass)
    {
        cachedDimensionTypes.addAll(DimensionDictionary.getDimensionTypes(provider));

        for (StructureInfo<?> structureInfo : structures)
        {
            List<T> generationInfos = structureInfo.generationInfos(typeClass);
            for (T selection : generationInfos)
            {
                double generationWeight = selection.getGenerationWeight(provider, biome);

                if (generationWeight > 0)
                    weightedStructureInfos.put(selection.generationCategory(), new WeightedSelector.SimpleItem<>(generationWeight, Pair.of(structureInfo, selection)));
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
        return weightedStructureInfos.get(c).stream()
                .mapToDouble(WeightedSelector.SimpleItem::getWeight)
                .sum();
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
