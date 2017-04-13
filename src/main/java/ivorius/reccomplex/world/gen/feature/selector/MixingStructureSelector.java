/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.selector;

import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.GenerationInfo;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by lukas on 24.09.16.
 */
public class MixingStructureSelector<T extends GenerationInfo & EnvironmentalSelection<C>, C extends MixingStructureSelector.Category> extends StructureSelector<T, C>
{
    public MixingStructureSelector(Map<String, Structure<?>> structures, WorldProvider provider, Biome biome, Class<T> typeClass)
    {
        super(structures, provider, biome, typeClass);
    }

    public int structuresInBiome(C category, WorldProvider worldProvider, Biome biome, Float distanceToSpawn, Random random)
    {
        return category != null ? category.structuresInBiome(biome, worldProvider, totalWeight(category), distanceToSpawn, random) : 0;
    }

    public List<Pair<Structure<?>, T>> generatedStructures(Random random, Biome biome, WorldProvider provider, Float distanceToSpawn)
    {
        return weightedStructureInfos.keySet().stream()
                .flatMap(category -> IntStream.range(0, structuresInBiome(category, provider, biome, distanceToSpawn, random)).mapToObj(i -> category))
                .map(category -> WeightedSelector.select(random, weightedStructureInfos.get(category)))
                .collect(Collectors.toList());
    }

    @Nullable
    public Pair<Structure<?>, T> selectOne(Random random, WorldProvider provider, Biome biome, @Nullable C c, Float distanceToSpawn)
    {
        if (c != null)
            return super.selectOne(random, c);

        List<WeightedSelector.SimpleItem<C>> list = weightedStructureInfos.keySet().stream()
                .map(category -> new WeightedSelector.SimpleItem<>(structuresInBiome(category, provider, biome, distanceToSpawn, random), category))
                .collect(Collectors.toList());

        return selectOne(random, WeightedSelector.select(random, list));
    }

    interface Category
    {
        int structuresInBiome(Biome biome, WorldProvider worldProvider, double totalWeight, Float distanceToSpawn, Random random);
    }
}
