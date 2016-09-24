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
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
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
                double generationWeight = selection.getGenerationWeight(biome, provider);

                if (generationWeight > 0)
                    weightedStructureInfos.put(selection.generationCategory(), new WeightedSelector.SimpleItem<>(generationWeight, Pair.of(structureInfo, selection)));
            }
        }
    }

    @Nullable
    public Pair<StructureInfo, T> selectOne(Random random, WorldProvider provider, Biome biome, @Nonnull C c)
    {
        return WeightedSelector.select(random, weightedStructureInfos.get(c));
    }

    public boolean isValid(Biome biome, WorldProvider provider)
    {
        return DimensionDictionary.getDimensionTypes(provider).equals(cachedDimensionTypes);
    }
}
