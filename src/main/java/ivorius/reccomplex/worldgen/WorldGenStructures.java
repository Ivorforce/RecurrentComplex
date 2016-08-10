/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen;

import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.StructureInfos;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.StaticGenerationInfo;
import ivorius.reccomplex.utils.BlockSurfacePos;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 24.05.14.
 */
public class WorldGenStructures implements IWorldGenerator
{
    public static void generateStaticStructuresInChunk(Random random, ChunkCoordIntPair chunkPos, World world, BlockPos spawnPos)
    {
        StructureRegistry.INSTANCE.getStaticStructuresAt(chunkPos, world, spawnPos).forEach(triple ->
        {
            StaticGenerationInfo staticGenInfo = triple.getMiddle();
            StructureInfo structureInfo = triple.getLeft();
            BlockSurfacePos pos = triple.getRight();
            String structureName = StructureRegistry.INSTANCE.structureID(structureInfo);

            RecurrentComplex.logger.trace(String.format("Spawning static structure at %s", pos));

            StructureGenerator.randomInstantly(world, random, structureInfo, staticGenInfo.ySelector, pos, false, structureName);
        });
    }

    public static void generateRandomStructuresInChunk(Random random, ChunkCoordIntPair chunkPos, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider, BiomeGenBase biomeGen)
    {
        StructureSelector structureSelector = StructureRegistry.INSTANCE.getStructureSelector(biomeGen, world.provider);

        List<Pair<StructureInfo, NaturalGenerationInfo>> generated = structureSelector.generatedStructures(random, chunkPos, world, chunkGenerator, chunkProvider);

        for (Pair<StructureInfo, NaturalGenerationInfo> pair : generated)
            generateStructureInChunk(random, chunkPos, world, pair);
    }

    public static void generateRandomStructureInChunk(Random random, ChunkCoordIntPair chunkPos, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider, BiomeGenBase biomeGen)
    {
        StructureSelector structureSelector = StructureRegistry.INSTANCE.getStructureSelector(biomeGen, world.provider);

        Pair<StructureInfo, NaturalGenerationInfo> pair = structureSelector.selectOne(random, chunkPos, world, chunkGenerator, chunkProvider);
        generateStructureInChunk(random, chunkPos, world, pair);
    }

    protected static void generateStructureInChunk(Random random, ChunkCoordIntPair chunkPos, World world, Pair<StructureInfo, NaturalGenerationInfo> pair)
    {
        StructureInfo structureInfo = pair.getLeft();
        NaturalGenerationInfo naturalGenInfo = pair.getRight();
        String structureName = StructureRegistry.INSTANCE.structureID(structureInfo);

        BlockSurfacePos genPos = new BlockSurfacePos(chunkPos.chunkXPos << 4 + random.nextInt(16), chunkPos.chunkZPos << 4 + random.nextInt(16));

        if (!naturalGenInfo.hasLimitations() || naturalGenInfo.getLimitations().areResolved(world, structureName))
            StructureGenerator.randomInstantly(world, random, structureInfo, naturalGenInfo.ySelector, genPos, true, structureName);
    }

    public static void generatePartialStructuresInChunk(Random random, final ChunkCoordIntPair chunkPos, final World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
    {
        StructureGenerationData data = StructureGenerationData.get(world);

        for (StructureGenerationData.Entry entry : data.getEntriesAt(chunkPos, true))
        {
            StructureInfo structureInfo = StructureRegistry.INSTANCE.getStructure(entry.getStructureID());

            if (structureInfo != null)
            {
                StructureGenerator.partially(structureInfo, world, random, entry.lowerCoord, entry.transform, StructureInfos.chunkBoundingBox(chunkPos), 0, entry.getStructureID(), entry.instanceData, entry.firstTime);

                if (entry.firstTime)
                {
                    entry.firstTime = false;
                    data.markDirty();
                }
            }
        }
    }

    @Override
    public void generate(Random random, final int chunkX, final int chunkZ, final World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
    {
        ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(chunkX, chunkZ);
        boolean worldWantsStructures = world.getWorldInfo().isMapFeaturesEnabled();
        StructureGenerationData data = StructureGenerationData.get(world);

        generatePartialStructuresInChunk(random, chunkPos, world, chunkGenerator, chunkProvider);

        if (!RCConfig.honorStructureGenerationOption || worldWantsStructures)
        {
            BiomeGenBase biomeGen = world.getBiomeGenForCoords(chunkPos.getBlock(8, 0, 8));
            BlockPos spawnPos = world.getSpawnPoint();

            generateStaticStructuresInChunk(random, chunkPos, world, spawnPos);

            if (data.checkChunk(chunkPos))
            {
                boolean mayGenerate = RCConfig.isGenerationEnabled(biomeGen) && RCConfig.isGenerationEnabled(world.provider);

                if (world.provider.getDimensionId() == 0)
                {
                    double distToSpawn = IvVecMathHelper.distanceSQ(new double[]{chunkPos.chunkXPos * 16 + 8, chunkPos.chunkZPos * 16 + 8}, new double[]{spawnPos.getX(), spawnPos.getZ()});
                    mayGenerate &= distToSpawn >= RCConfig.minDistToSpawnForGeneration * RCConfig.minDistToSpawnForGeneration;
                }

                if (mayGenerate)
                    generateRandomStructuresInChunk(random, chunkPos, world, chunkGenerator, chunkProvider, biomeGen);
            }
        }
    }
}
