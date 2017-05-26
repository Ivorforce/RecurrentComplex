/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature;

import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.reccomplex.world.gen.feature.selector.MixingStructureSelector;
import ivorius.reccomplex.world.gen.feature.selector.NaturalStructureSelector;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.Structures;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.NaturalGeneration;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.StaticGeneration;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Created by lukas on 24.05.14.
 */
public class WorldGenStructures
{

    public static final int STRUCTURE_TRIES = 10;

    public static void planStaticStructuresInChunk(Random random, ChunkPos chunkPos, WorldServer world, BlockPos spawnPos, @Nullable Predicate<Structure> structurePredicate)
    {
        StructureRegistry.INSTANCE.getStaticStructuresAt(chunkPos, world, spawnPos).forEach(triple ->
        {
            StaticGeneration staticGenInfo = triple.getMiddle();
            Structure<?> structure = triple.getLeft();
            BlockSurfacePos pos = triple.getRight();

            if (structurePredicate != null && !structurePredicate.test(structure))
                return;

            new StructureGenerator<>(structure).world(world).generationInfo(staticGenInfo)
                    .random(random).randomPosition(pos, staticGenInfo.placer.getContents()).fromCenter(true)
                    .partially(RecurrentComplex.PARTIALLY_SPAWN_NATURAL_STRUCTURES, chunkPos)
                    .generate();
        });
    }

    protected static float distance(ChunkPos left, ChunkPos right)
    {
        return MathHelper.sqrt_float(
                (left.chunkXPos - right.chunkXPos) * (left.chunkXPos - right.chunkXPos) +
                        (left.chunkZPos - right.chunkZPos) * (left.chunkZPos - right.chunkZPos));
    }

    public static void planStructuresInChunk(Random random, ChunkPos chunkPos, WorldServer world, Biome biomeGen, @Nullable Predicate<Structure> structurePredicate)
    {
        MixingStructureSelector<NaturalGeneration, NaturalStructureSelector.Category> structureSelector = StructureRegistry.INSTANCE.naturalStructureSelectors().get(biomeGen, world.provider);

        float distanceToSpawn = distance(new ChunkPos(world.getSpawnPoint()), chunkPos);
        // TODO Use STRUCTURE_TRIES
        List<Pair<Structure<?>, NaturalGeneration>> generated = structureSelector.generatedStructures(random, world.getBiome(chunkPos.getBlock(0, 0, 0)), world.provider, distanceToSpawn);

        generated.stream()
                .filter(pair -> structurePredicate == null || structurePredicate.test(pair.getLeft()))
                .forEach(pair -> planStructureInChunk(random, chunkPos, world, pair.getLeft(), pair.getRight(), false));
    }

    public static boolean generateRandomStructureInChunk(Random random, ChunkPos chunkPos, WorldServer world, Biome biomeGen)
    {
        MixingStructureSelector<NaturalGeneration, NaturalStructureSelector.Category> structureSelector = StructureRegistry.INSTANCE.naturalStructureSelectors().get(biomeGen, world.provider);

        float distanceToSpawn = distance(new ChunkPos(world.getSpawnPoint()), chunkPos);

        for (int i = 0; i < STRUCTURE_TRIES; i++)
        {
            Pair<Structure<?>, NaturalGeneration> pair = structureSelector.selectOne(random, world.provider, world.getBiome(chunkPos.getBlock(0, 0, 0)), null, distanceToSpawn);

            if (pair != null)
            {
                if (planStructureInChunk(random, chunkPos, world, pair.getLeft(), pair.getRight(), true))
                    return true;
            }
        }

        return false;
    }

    // TODO Use !instantly to only plan structure but later generate
    protected static boolean planStructureInChunk(Random random, ChunkPos chunkPos, WorldServer world, Structure<?> structure, NaturalGeneration naturalGenInfo, boolean instantly)
    {
        String structureName = StructureRegistry.INSTANCE.id(structure);

        BlockSurfacePos genPos = new BlockSurfacePos((chunkPos.chunkXPos << 4) + 8 + random.nextInt(16), (chunkPos.chunkZPos << 4) + 8 + random.nextInt(16));

        if (!naturalGenInfo.hasLimitations() || naturalGenInfo.getLimitations().areResolved(world, structureName))
        {
            StructureGenerator<?> generator = new StructureGenerator<>(structure).world(world).generationInfo(naturalGenInfo)
                    .random(random).maturity(StructureSpawnContext.GenerateMaturity.SUGGEST)
                    .randomPosition(genPos, naturalGenInfo.placer.getContents()).fromCenter(true)
                    .partially(RecurrentComplex.PARTIALLY_SPAWN_NATURAL_STRUCTURES, chunkPos);

            if (naturalGenInfo.getGenerationWeight(world.provider, generator.environment().biome) <= 0)
            {
                RecurrentComplex.logger.trace(String.format("%s failed to spawn at %s (incompatible biome edge)", structure, genPos));
                return false;
            }

            return generator.generate().isPresent();
        }

        return false;
    }

    public static void complementStructuresInChunk(Random random, final ChunkPos chunkPos, final WorldServer world)
    {
        WorldStructureGenerationData data = WorldStructureGenerationData.get(world);

        // Don't filter hasBeenGenerated since if the chunk re-generates now, we want to complement our structure back anyway
        data.structureEntriesIn(chunkPos).forEach(entry -> {
            Structure<?> structure = StructureRegistry.INSTANCE.get(entry.getStructureID());

            if (structure == null)
            {
                RecurrentComplex.logger.warn(String.format("Can't find structure %s (%s) to complement in %s (%d)", entry.getStructureID(), entry.getUuid(), chunkPos, world.provider.getDimension()));
                return;
            }

            if (entry.instanceData == null)
            {
                RecurrentComplex.logger.warn(String.format("Can't find instance data of %s (%s) to complement in %s (%d)", entry.getStructureID(), entry.getUuid(), chunkPos, world.provider.getDimension()));
                return;
            }

            new StructureGenerator<>(structure).world(world).generationInfo(entry.generationInfoID)
                    .random(random).boundingBox(entry.boundingBox).transform(entry.transform).generationBB(Structures.chunkBoundingBox(chunkPos))
                    .structureID(entry.getStructureID()).instanceData(entry.instanceData)
                    // Could use entry.firstTime but then StructureGenerator would add a new entry
                    .maturity(StructureSpawnContext.GenerateMaturity.COMPLEMENT)
                    .generate();

            if (entry.firstTime)
            {
                entry.firstTime = false;
                data.markDirty();
            }
        });
    }

    public static boolean decorate(WorldServer world, Random random, ChunkPos chunkPos, @Nullable Predicate<Structure> structurePredicate)
    {
        boolean generated = false;

        boolean worldWantsStructures = world.getWorldInfo().isMapFeaturesEnabled();
        WorldStructureGenerationData data = WorldStructureGenerationData.get(world);

        // We need to synchronize (multithreaded gen) since we need to plan structures before complementing,
        // otherwise structures get lost in some chunks
        synchronized (data)
        {
            // TODO Synchronize on chunk pos instead (need to make sure these are only added on same sync though)
            // If not partially, complement before generating so we don't generate structures twice
            if (!RecurrentComplex.PARTIALLY_SPAWN_NATURAL_STRUCTURES && structurePredicate == null)
                complementStructuresInChunk(random, chunkPos, world);

            if ((!RCConfig.honorStructureGenerationOption || worldWantsStructures)
                    // If partially spawn, check chunks as having tried to add partial structures as into the thingy
                    && (structurePredicate == null || !RecurrentComplex.PARTIALLY_SPAWN_NATURAL_STRUCTURES || data.checkChunkFinal(chunkPos)))
            {
                if (structurePredicate == null)
                    data.checkChunk(chunkPos);

                Biome biomeGen = world.getBiome(chunkPos.getBlock(8, 0, 8));
                BlockPos spawnPos = world.getSpawnPoint();

                planStaticStructuresInChunk(random, chunkPos, world, spawnPos, structurePredicate);

                boolean mayGenerate = RCConfig.isGenerationEnabled(biomeGen) && RCConfig.isGenerationEnabled(world.provider);

                if (world.provider.getDimension() == 0)
                {
                    double distToSpawn = IvVecMathHelper.distanceSQ(new double[]{chunkPos.chunkXPos * 16 + 8, chunkPos.chunkZPos * 16 + 8}, new double[]{spawnPos.getX(), spawnPos.getZ()});
                    mayGenerate &= distToSpawn >= RCConfig.minDistToSpawnForGeneration * RCConfig.minDistToSpawnForGeneration;
                }

                if (mayGenerate)
                    planStructuresInChunk(random, chunkPos, world, biomeGen, structurePredicate);

                generated = true;
            }

            if (RecurrentComplex.PARTIALLY_SPAWN_NATURAL_STRUCTURES && structurePredicate == null)
                complementStructuresInChunk(random, chunkPos, world);
        }

        return generated;
    }
}
