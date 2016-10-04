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
import ivorius.reccomplex.utils.BlockSurfacePos;
import ivorius.reccomplex.utils.Chunks;
import ivorius.reccomplex.world.gen.feature.decoration.RCBiomeDecorator;
import ivorius.reccomplex.world.gen.feature.selector.CachedStructureSelectors;
import ivorius.reccomplex.world.gen.feature.selector.MixingStructureSelector;
import ivorius.reccomplex.world.gen.feature.selector.NaturalStructureSelector;
import ivorius.reccomplex.world.gen.feature.selector.StructureSelector;
import ivorius.reccomplex.world.gen.feature.villages.GenericVillageCreationHandler;
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
public class StructureRegistry extends SimpleLeveledRegistry<StructureInfo>
{
    public static final StructureRegistry INSTANCE = new StructureRegistry();

    public static SerializableStringTypeRegistry<Transformer> TRANSFORMERS = new SerializableStringTypeRegistry<>("transformer", "type", Transformer.class);
    public static SerializableStringTypeRegistry<StructureGenerationInfo> GENERATION_INFOS = new SerializableStringTypeRegistry<>("generationInfo", "type", StructureGenerationInfo.class);

    private Map<Class<? extends StructureGenerationInfo>, Collection<Pair<StructureInfo, ? extends StructureGenerationInfo>>> cachedGeneration = new HashMap<>();

    private CachedStructureSelectors<MixingStructureSelector<NaturalGenerationInfo, NaturalStructureSelector.Category>> naturalSelectors
            = new CachedStructureSelectors<>((biome, worldProvider) ->
            new MixingStructureSelector<>(this.allActive(), worldProvider, biome, NaturalGenerationInfo.class));

    private CachedStructureSelectors<StructureSelector<VanillaDecorationGenerationInfo, RCBiomeDecorator.DecorationType>> decorationSelectors
            = new CachedStructureSelectors<>((biome, worldProvider) ->
            new StructureSelector<>(this.allActive(), worldProvider, biome, VanillaDecorationGenerationInfo.class));

    public StructureRegistry()
    {
        super("structure");
    }

    @Override
    public StructureInfo register(String id, String domain, StructureInfo structureInfo, boolean active, ILevel level)
    {
        if (active && !(RCConfig.shouldStructureGenerate(id, domain) && structureInfo.areDependenciesResolved()))
            active = false;

        clearCaches();

        return super.register(id, domain, structureInfo, active, level);
    }

    @Override
    public StructureInfo unregister(String id, ILevel level)
    {
        clearCaches();
        return super.unregister(id, level);
    }

    protected <T extends StructureGenerationInfo> Collection<Pair<StructureInfo, T>> getCachedGeneration(Class<T> clazz)
    {
        return (Collection<Pair<StructureInfo, T>>) ((Map) cachedGeneration).get(clazz);
    }

    public <T extends StructureGenerationInfo> Collection<Pair<StructureInfo, T>> getStructureGenerations(Class<T> clazz)
    {
        Collection<Pair<StructureInfo, T>> pairs = getCachedGeneration(clazz);
        if (pairs != null)
            return pairs;

        pairs = new ArrayList<>();
        for (StructureInfo info : this.allActive())
        {
            List<T> generationInfos = info.generationInfos(clazz);
            for (T t : generationInfos)
                pairs.add(Pair.of(info, t));
        }

        ((ArrayList) pairs).trimToSize();
        cachedGeneration.put(clazz, (Collection) pairs);

        return pairs;
    }

    public <T extends StructureGenerationInfo> Collection<Pair<StructureInfo, T>> getStructureGenerations(Class<T> clazz, final Predicate<Pair<StructureInfo, T>> predicate)
    {
        return Collections2.filter(getStructureGenerations(clazz), predicate::test);
    }

    public Collection<Pair<StructureInfo, StructureListGenerationInfo>> getStructuresInList(final String listID, @Nullable final EnumFacing front)
    {
        return getStructureGenerations(StructureListGenerationInfo.class, input -> listID.equals(input.getRight().listID)
                && (front == null || input.getLeft().isRotatable() || input.getRight().front == front));
    }

    public Collection<Pair<StructureInfo, MazeGenerationInfo>> getStructuresInMaze(final String mazeID)
    {
        return getStructureGenerations(MazeGenerationInfo.class, input ->
        {
            MazeGenerationInfo info = input.getRight();
            return mazeID.equals(info.mazeID) && info.mazeComponent.isValid();
        });
    }

    public Stream<Triple<StructureInfo, StaticGenerationInfo, BlockSurfacePos>> getStaticStructuresAt(ChunkPos chunkPos, final World world, final BlockPos spawnPos)
    {
        Collection<Pair<StructureInfo, StaticGenerationInfo>> statics = getStructureGenerations(StaticGenerationInfo.class, input ->
        {
            StaticGenerationInfo info = input.getRight();

            return info.dimensionMatcher.test(world.provider)
                    && (info.pattern != null || Chunks.contains(chunkPos, info.getPos(spawnPos)));
        });

        return statics.stream().flatMap(pair ->
        {
            StaticGenerationInfo info = pair.getRight();
            return info.hasPattern()
                    ? Chunks.repeatIntersections(chunkPos, info.getPos(spawnPos), info.pattern.repeatX, info.pattern.repeatZ).map(pos -> Triple.of(pair.getLeft(), info, pos))
                    : Stream.of(Triple.of(pair.getLeft(), info, info.getPos(spawnPos)));
        });
    }

    public CachedStructureSelectors<MixingStructureSelector<NaturalGenerationInfo, NaturalStructureSelector.Category>> naturalStructureSelectors()
    {
        return naturalSelectors;
    }

    public CachedStructureSelectors<StructureSelector<VanillaDecorationGenerationInfo, RCBiomeDecorator.DecorationType>> decorationSelectors()
    {
        return decorationSelectors;
    }

    private void clearCaches()
    {
        naturalSelectors.clear();
        decorationSelectors.clear();
        cachedGeneration.clear();

        updateVanillaGenerations();
        for (Pair<StructureInfo, VanillaStructureGenerationInfo> pair : getStructureGenerations(VanillaStructureGenerationInfo.class))
        {
            String structureID = this.id(pair.getLeft());
            String generationID = pair.getRight().id();
            Class clazz = GenericVillageCreationHandler.getPieceClass(structureID, generationID);
            if (clazz != null)
                MapGenStructureIO.registerStructureComponent(clazz, "Rc:" + structureID + "_" + generationID);
        }
    }

    private void updateVanillaGenerations()
    {
        TemporaryVillagerRegistry.instance().setHandlers(
                Sets.newHashSet(Collections2.transform(getStructureGenerations(VanillaStructureGenerationInfo.class),
                        input ->
                        {
                            return GenericVillageCreationHandler.forGeneration(this.id(input.getLeft()), input.getRight().id());
                        }).stream().filter(Objects::nonNull).collect(Collectors.toList()))
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
