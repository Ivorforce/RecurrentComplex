/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.worldgen.selector;

import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.generic.gentypes.EnvironmentalSelection;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by lukas on 23.09.16.
 */
public class CachedStructureSelectors<T extends StructureGenerationInfo & EnvironmentalSelection<C>, C extends StructureSelector.Category>
{
    private Map<Pair<Integer, ResourceLocation>, StructureSelector<T, C>> structureSelectors = new HashMap<>();

    private Supplier<Collection<StructureInfo>> structureSupplier;

    public CachedStructureSelectors(Supplier<Collection<StructureInfo>> structureSupplier)
    {
        this.structureSupplier = structureSupplier;
    }

    public StructureSelector<T, C> get(Biome biome, WorldProvider provider)
    {
        Pair<Integer, ResourceLocation> pair = new ImmutablePair<>(provider.getDimension(), Biome.REGISTRY.getNameForObject(biome));
        StructureSelector<T, C> structureSelector = structureSelectors.get(pair);

        if (structureSelector == null || !structureSelector.isValid(biome, provider))
        {
            structureSelector = new StructureSelector(generatingStructures(), biome, provider, NaturalGenerationInfo.class);
            structureSelectors.put(pair, structureSelector);
        }

        return structureSelector;
    }

    protected Collection<StructureInfo> generatingStructures()
    {
        return structureSupplier.get();
    }

    public void clear()
    {
        structureSelectors.clear();
    }
}
