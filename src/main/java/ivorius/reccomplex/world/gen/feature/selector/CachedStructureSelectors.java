/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.selector;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Created by lukas on 23.09.16.
 */
public class CachedStructureSelectors<S extends StructureSelector>
{
    private Map<Pair<Integer, ResourceLocation>, S> structureSelectors = new HashMap<>();

    private BiFunction<Biome, WorldProvider, S> selectorSupplier;

    public CachedStructureSelectors(BiFunction<Biome, WorldProvider, S> selectorSupplier)
    {
        this.selectorSupplier = selectorSupplier;
    }

    public S get(Biome biome, WorldProvider provider)
    {
        Pair<Integer, ResourceLocation> pair = new ImmutablePair<>(provider.getDimension(), Biome.REGISTRY.getNameForObject(biome));
        S structureSelector = structureSelectors.get(pair);

        if (structureSelector == null || !structureSelector.isValid(biome, provider))
        {
            structureSelector = selectorSupplier.apply(biome, provider);
            structureSelectors.put(pair, structureSelector);
        }

        return structureSelector;
    }

    public void clear()
    {
        structureSelectors.clear();
    }
}
