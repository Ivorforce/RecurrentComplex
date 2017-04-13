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
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.*;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.Transformer;
import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.ivtoolkit.world.chunk.Chunks;
import ivorius.reccomplex.world.gen.feature.decoration.RCBiomeDecorator;
import ivorius.reccomplex.world.gen.feature.selector.CachedStructureSelectors;
import ivorius.reccomplex.world.gen.feature.selector.MixingStructureSelector;
import ivorius.reccomplex.world.gen.feature.selector.NaturalStructureSelector;
import ivorius.reccomplex.world.gen.feature.selector.StructureSelector;
import ivorius.reccomplex.world.gen.feature.villages.GenericVillageCreationHandler;
import ivorius.reccomplex.world.gen.feature.villages.GenericVillagePiece;
import ivorius.reccomplex.world.gen.feature.villages.TemporaryVillagerRegistry;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by lukas on 24.05.14.
 */
public class StructureRegistry extends SimpleLeveledRegistry<Structure<?>>
{
    public static final StructureRegistry INSTANCE = new StructureRegistry();

    public static SerializableStringTypeRegistry<Transformer> TRANSFORMERS = new SerializableStringTypeRegistry<>("transformer", "type", Transformer.class);
    public static SerializableStringTypeRegistry<GenerationType> GENERATION_INFOS = new SerializableStringTypeRegistry<>("generationInfo", "type", GenerationType.class);

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

    public <T extends GenerationType> Collection<Pair<Structure<?>, T>> getStructureGenerations(Class<T> clazz)
    {
        Collection<Pair<Structure<?>, T>> pairs = getCachedGeneration(clazz);
        if (pairs != null)
            return pairs;

        pairs = new ArrayList<>();
        for (Structure<?> info : this.allActive())
        {
            List<T> generationInfos = info.generationInfos(clazz);
            for (T t : generationInfos)
                pairs.add(Pair.of(info, t));
        }

        ((ArrayList) pairs).trimToSize();
        //noinspection unchecked
        cachedGeneration.put(clazz, (Collection) pairs);

        return pairs;
    }

    public Stream<Pair<Structure<?>, ListGeneration>> getStructuresInList(final String listID, @Nullable final EnumFacing front)
    {
        final Predicate<Pair<Structure<?>, ListGeneration>> predicate = input -> listID.equals(input.getRight().listID)
                && (front == null || input.getLeft().isRotatable() || input.getRight().front == front);
        return getStructureGenerations(ListGeneration.class).stream().filter(predicate);
    }

    public Stream<Pair<Structure<?>, MazeGeneration>> getStructuresInMaze(final String mazeID)
    {
        final Predicate<Pair<Structure<?>, MazeGeneration>> predicate = input ->
        {
            MazeGeneration info = input.getRight();
            return mazeID.equals(info.mazeID) && info.mazeComponent.isValid();
        };
        return getStructureGenerations(MazeGeneration.class).stream().filter(predicate);
    }

    public Stream<Triple<Structure<?>, StaticGeneration, BlockSurfacePos>> getStaticStructuresAt(ChunkPos chunkPos, final World world, final BlockPos spawnPos)
    {
        final Predicate<Pair<Structure<?>, StaticGeneration>> predicate = input ->
        {
            StaticGeneration info = input.getRight();

            return info.dimensionMatcher.test(world.provider)
                    && (info.pattern != null || Chunks.contains(chunkPos, info.getPos(spawnPos)));
        };
        Stream<Pair<Structure<?>, StaticGeneration>> statics = getStructureGenerations(StaticGeneration.class).stream().filter(predicate);

        return statics.flatMap(pair ->
        {
            StaticGeneration info = pair.getRight();
            //noinspection ConstantConditions
            return info.hasPattern()
                    ? Chunks.repeatIntersections(chunkPos, info.getPos(spawnPos), info.pattern.repeatX, info.pattern.repeatZ).map(pos -> Triple.of(pair.getLeft(), info, pos))
                    : Stream.of(Triple.of(pair.getLeft(), info, info.getPos(spawnPos)));
        });
    }

    public CachedStructureSelectors<MixingStructureSelector<NaturalGeneration, NaturalStructureSelector.Category>> naturalStructureSelectors()
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
        for (Pair<Structure<?>, VanillaGeneration> pair : getStructureGenerations(VanillaGeneration.class))
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
                Sets.newHashSet(Collections2.transform(getStructureGenerations(VanillaGeneration.class),
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
