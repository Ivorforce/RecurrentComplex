/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure;

import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.files.SimpleLeveledRegistry;
import ivorius.reccomplex.json.SerializableStringTypeRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.Transformer;
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
    private Map<Class<? extends GenerationCache>, GenerationCache> generationCaches = new HashMap<>();

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

    public <T extends GenerationCache> void registerCache(Class<T> type, T cache)
    {
        generationCaches.put(type, cache);
        cache.setRegistry(this);
    }

    public <T extends GenerationCache> void registerCache(T cache)
    {
        //noinspection unchecked
        registerCache((Class<T>) cache.getClass(), cache);
    }

    public <T extends GenerationCache> T getCache(Class<T> cache)
    {
        //noinspection unchecked
        return (T) generationCaches.get(cache);
    }

    protected <T extends GenerationType> Collection<Pair<Structure<?>, T>> getCachedGeneration(Class<T> clazz)
    {
        //noinspection unchecked
        return (Collection<Pair<Structure<?>, T>>) ((Map) cachedGeneration).get(clazz);
    }

    public <T extends GenerationType> Collection<Pair<Structure<?>, T>> getGenerationTypes(Class<T> clazz)
    {
        Collection<Pair<Structure<?>, T>> pairs = getCachedGeneration(clazz);

        if (pairs == null)
        {
            pairs = allActive().stream()
                    .flatMap(s -> s.generationTypes(clazz).stream()
                            .<Pair<Structure<?>, T>>map(t -> Pair.of(s, t)))
                    .collect(Collectors.toList());

            //noinspection unchecked
            cachedGeneration.put(clazz, (Collection) pairs);
        }

        return pairs;
    }

    private void clearCaches()
    {
        cachedGeneration.clear();
        generationCaches.values().forEach(GenerationCache::clear);
    }

    public interface GenerationCache
    {
        void setRegistry(StructureRegistry registry);

        void clear();
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
