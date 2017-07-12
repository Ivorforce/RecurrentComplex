/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure;

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.files.SimpleLeveledRegistry;
import ivorius.reccomplex.json.SerializableStringTypeRegistry;
import ivorius.reccomplex.world.gen.feature.decoration.RCBiomeDecorator;
import ivorius.reccomplex.world.gen.feature.selector.CachedStructureSelectors;
import ivorius.reccomplex.world.gen.feature.selector.MixingStructureSelector;
import ivorius.reccomplex.world.gen.feature.selector.NaturalStructureSelector;
import ivorius.reccomplex.world.gen.feature.selector.StructureSelector;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.NaturalGeneration;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.VanillaDecorationGeneration;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.VanillaGeneration;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.Transformer;
import ivorius.reccomplex.world.gen.feature.villages.GenericVillageCreationHandler;
import ivorius.reccomplex.world.gen.feature.villages.GenericVillagePiece;
import ivorius.reccomplex.world.gen.feature.villages.TemporaryVillagerRegistry;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 24.05.14.
 */
public class StructureRegistry extends SimpleLeveledRegistry<Structure<?>>
{
    public static final StructureRegistry INSTANCE = new StructureRegistry();

    public static SerializableStringTypeRegistry<Transformer> TRANSFORMERS = new SerializableStringTypeRegistry<>("transformer", "type", Transformer.class);
    public static SerializableStringTypeRegistry<GenerationType> GENERATION_TYPES = new SerializableStringTypeRegistry<>("generationInfo", "type", GenerationType.class);

    private Map<Class<? extends GenerationType>, Collection<Pair<Structure<?>, ? extends GenerationType>>> cachedGeneration = new HashMap<>();

    private CachedStructureSelectors<MixingStructureSelector<NaturalGeneration, NaturalStructureSelector.Category>> naturalSelectors
            = new CachedStructureSelectors<>((biome, worldProvider) ->
            new MixingStructureSelector<>(this.activeMap(), worldProvider, biome, NaturalGeneration.class));

    private CachedStructureSelectors<StructureSelector<VanillaDecorationGeneration, RCBiomeDecorator.DecorationType>> decorationSelectors
            = new CachedStructureSelectors<>((biome, worldProvider) ->
            new StructureSelector<>(this.activeMap(), worldProvider, biome, VanillaDecorationGeneration.class));

    public StructureRegistry()
    {
        super("structure");
    }

    @Override
    public Structure register(String id, String domain, Structure structure, boolean active, ILevel level)
    {
        if (active && !(RCConfig.shouldStructureGenerate(id, domain) && structure.areDependenciesResolved()))
            active = false;

        Structure prev = super.register(id, domain, structure, active, level);

        clearCaches();

        return prev;
    }

    @Override
    public Structure unregister(String id, ILevel level)
    {
        clearCaches();
        return super.unregister(id, level);
    }

    protected <T extends GenerationType> Collection<Pair<Structure<?>, T>> getCachedGeneration(Class<T> clazz)
    {
        //noinspection unchecked
        return (Collection<Pair<Structure<?>, T>>) ((Map) cachedGeneration).get(clazz);
    }

    public <T extends GenerationType> Collection<Pair<Structure<?>, T>> getGenerationTypes(Class<T> clazz)
    {
        Collection<Pair<Structure<?>, T>> pairs = getCachedGeneration(clazz);
        if (pairs != null)
            return pairs;

        pairs = new ArrayList<>();
        for (Structure<?> info : this.allActive())
        {
            List<T> generationInfos = info.generationTypes(clazz);
            for (T t : generationInfos)
                pairs.add(Pair.of(info, t));
        }

        ((ArrayList) pairs).trimToSize();
        //noinspection unchecked
        cachedGeneration.put(clazz, (Collection) pairs);

        return pairs;
    }

    public CachedStructureSelectors<MixingStructureSelector<NaturalGeneration, NaturalStructureSelector.Category>> naturalSelectors()
    {
        return naturalSelectors;
    }

    public CachedStructureSelectors<StructureSelector<VanillaDecorationGeneration, RCBiomeDecorator.DecorationType>> decorationSelectors()
    {
        return decorationSelectors;
    }

    private void clearCaches()
    {
        naturalSelectors.clear();
        decorationSelectors.clear();
        cachedGeneration.clear();

        updateVanillaGenerations();
        for (Pair<Structure<?>, VanillaGeneration> pair : getGenerationTypes(VanillaGeneration.class))
        {
            String structureID = this.id(pair.getLeft());
            String generationID = pair.getRight().id();
            Class<? extends GenericVillagePiece> clazz = GenericVillageCreationHandler.getPieceClass(structureID, generationID);
            if (clazz != null)
                MapGenStructureIO.registerStructureComponent(clazz, "Rc:" + structureID + "_" + generationID);
        }
    }

    private void updateVanillaGenerations()
    {
        TemporaryVillagerRegistry.instance().setHandlers(
                Sets.newHashSet(Collections2.transform(getGenerationTypes(VanillaGeneration.class),
                        input -> GenericVillageCreationHandler.forGeneration(this.id(input.getLeft()), input.getRight().id())).stream()
                        .filter(Objects::nonNull).collect(Collectors.toList()))
        );
    }

    private static class StructureData
    {
        public boolean disabled;
        public String domain;

        public StructureData(boolean disabled, String domain)
        {
            this.disabled = disabled;
            this.domain = domain;
        }
    }
}
