/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen;

import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.structures.*;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.StaticGenerationInfo;
import ivorius.reccomplex.utils.BlockSurfacePos;
import ivorius.reccomplex.worldgen.selector.NaturalStructureSelector;
import ivorius.reccomplex.worldgen.selector.StructureSelector;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 24.05.14.
 */
public class WorldGenStructures
{

    public static void generateStaticStructuresInChunk(Random random, ChunkPos chunkPos, WorldServer world, BlockPos spawnPos)
    {
        StructureRegistry.INSTANCE.getStaticStructuresAt(chunkPos, world, spawnPos).forEach(triple ->
        {
            StaticGenerationInfo staticGenInfo = triple.getMiddle();
            StructureInfo structureInfo = triple.getLeft();
            BlockSurfacePos pos = triple.getRight();

            RecurrentComplex.logger.trace(String.format("Spawning static structure at %s", pos));

            new StructureGenerator<>(structureInfo).world(world).generationInfo(staticGenInfo)
                    .random(random).randomPosition(pos, staticGenInfo.placer.getContents()).fromCenter(true).generate();
        });
    }

    public static void generateRandomStructuresInChunk(Random random, ChunkPos chunkPos, WorldServer world, Biome biomeGen)
    {
        StructureSelector<NaturalGenerationInfo, NaturalStructureSelector.Category> structureSelector = StructureRegistry.INSTANCE.naturalStructureSelectors().get(biomeGen, world.provider);

        List<Pair<StructureInfo, NaturalGenerationInfo>> generated = structureSelector.generatedStructures(random, chunkPos, world);

        for (Pair<StructureInfo, NaturalGenerationInfo> pair : generated)
            generateStructureInChunk(random, chunkPos, world, pair);
    }

    public static boolean generateRandomStructureInChunk(Random random, ChunkPos chunkPos, WorldServer world, Biome biomeGen)
    {
        StructureSelector<NaturalGenerationInfo, NaturalStructureSelector.Category> structureSelector = StructureRegistry.INSTANCE.naturalStructureSelectors().get(biomeGen, world.provider);

        Pair<StructureInfo, NaturalGenerationInfo> pair = structureSelector.selectOne(random, chunkPos, world);

        if (pair != null)
        {
            generateStructureInChunk(random, chunkPos, world, pair);
            return true;
        }

        return false;
    }

    protected static void generateStructureInChunk(Random random, ChunkPos chunkPos, WorldServer world, Pair<StructureInfo, NaturalGenerationInfo> pair)
    {
        StructureInfo structureInfo = pair.getLeft();
        NaturalGenerationInfo naturalGenInfo = pair.getRight();
        String structureName = StructureRegistry.INSTANCE.structureID(structureInfo);

        BlockSurfacePos genPos = new BlockSurfacePos((chunkPos.chunkXPos << 4) + 8 + random.nextInt(16), (chunkPos.chunkZPos << 4) + 8 + random.nextInt(16));

        if (!naturalGenInfo.hasLimitations() || naturalGenInfo.getLimitations().areResolved(world, structureName))
        {
            new StructureGenerator<>(structureInfo).world(world).generationInfo(naturalGenInfo)
                    .random(random).maturity(StructureSpawnContext.GenerateMaturity.SUGGEST)
                    .randomPosition(genPos, naturalGenInfo.placer.getContents()).fromCenter(true).generate();
        }
    }

    public static void generatePartialStructuresInChunk(Random random, final ChunkPos chunkPos, final WorldServer world)
    {
        StructureGenerationData data = StructureGenerationData.get(world);

        for (StructureGenerationData.Entry entry : data.getEntriesAt(chunkPos, true))
        {
            StructureInfo structureInfo = StructureRegistry.INSTANCE.getStructure(entry.getStructureID());

            if (structureInfo != null)
            {
                new StructureGenerator<>(structureInfo).world(world).generationInfo(entry.generationInfoID)
                        .random(random).lowerCoord(entry.lowerCoord).transform(entry.transform).generationBB(StructureInfos.chunkBoundingBox(chunkPos))
                        .structureID(entry.getStructureID()).instanceData(entry.instanceData).maturity(entry.firstTime ? StructureSpawnContext.GenerateMaturity.FIRST : StructureSpawnContext.GenerateMaturity.COMPLEMENT).generate();

                if (entry.firstTime)
                {
                    entry.firstTime = false;
                    data.markDirty();
                }
            }
        }
    }

    public void generate(Random random, int chunkX, int chunkZ, WorldServer world)
    {
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        boolean worldWantsStructures = world.getWorldInfo().isMapFeaturesEnabled();
        StructureGenerationData data = StructureGenerationData.get(world);

        generatePartialStructuresInChunk(random, chunkPos, world);

        if (!RCConfig.honorStructureGenerationOption || worldWantsStructures)
        {
            Biome biomeGen = world.getBiome(chunkPos.getBlock(8, 0, 8));
            BlockPos spawnPos = world.getSpawnPoint();

            generateStaticStructuresInChunk(random, chunkPos, world, spawnPos);

            if (data.checkChunk(chunkPos))
            {
                boolean mayGenerate = RCConfig.isGenerationEnabled(biomeGen) && RCConfig.isGenerationEnabled(world.provider);

                if (world.provider.getDimension() == 0)
                {
                    double distToSpawn = IvVecMathHelper.distanceSQ(new double[]{chunkPos.chunkXPos * 16 + 8, chunkPos.chunkZPos * 16 + 8}, new double[]{spawnPos.getX(), spawnPos.getZ()});
                    mayGenerate &= distToSpawn >= RCConfig.minDistToSpawnForGeneration * RCConfig.minDistToSpawnForGeneration;
                }

                if (mayGenerate)
                    generateRandomStructuresInChunk(random, chunkPos, world, biomeGen);
            }
        }
    }
}
