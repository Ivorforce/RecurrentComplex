/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen;

import net.minecraftforge.fml.common.IWorldGenerator;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.StructureInfos;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.StaticGenerationInfo;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 24.05.14.
 */
public class WorldGenStructures implements IWorldGenerator
{
    @Override
    public void generate(Random random, final int chunkX, final int chunkZ, final World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
    {
        boolean worldWantsStructures = world.getWorldInfo().isMapFeaturesEnabled();
        StructureGenerationData data = StructureGenerationData.get(world);

        generatePartialStructuresInChunk(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);

        if (!RCConfig.honorStructureGenerationOption || worldWantsStructures)
        {
            BiomeGenBase biomeGen = world.getBiomeGenForCoords(new BlockPos(chunkX * 16, 0, chunkZ * 16));
            BlockPos spawnPos = world.getSpawnPoint();

            generateStaticStructuresInChunk(random, chunkX, chunkZ, world, spawnPos);

            if (data.checkChunk(new ChunkCoordIntPair(chunkX, chunkZ)))
            {
                boolean mayGenerate = RCConfig.isGenerationEnabled(biomeGen) && RCConfig.isGenerationEnabled(world.provider);

                if (world.provider.getDimensionId() == 0)
                {
                    double distToSpawn = IvVecMathHelper.distanceSQ(new double[]{chunkX * 16 + 8, chunkZ * 16 + 8}, new double[]{spawnPos.getX(), spawnPos.getZ()});
                    mayGenerate &= distToSpawn >= RCConfig.minDistToSpawnForGeneration * RCConfig.minDistToSpawnForGeneration;
                }

                if (mayGenerate)
                    generateRandomStructuresInChunk(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider, biomeGen);
            }
        }
    }

    public static void generateStaticStructuresInChunk(Random random, int chunkX, int chunkZ, World world, BlockPos spawnPos)
    {
        for (Pair<StructureInfo, StaticGenerationInfo> pair : StructureRegistry.INSTANCE.getStaticStructuresAt(chunkX, chunkZ, world, spawnPos))
        {
            RecurrentComplex.logger.trace(String.format("Spawning static structure at x = %d, z = %d", chunkX << 4, chunkZ << 4));

            StaticGenerationInfo staticGenInfo = pair.getRight();
            StructureInfo structureInfo = pair.getLeft();
            String structureName = StructureRegistry.INSTANCE.structureID(structureInfo);

            int strucX = staticGenInfo.getPositionX(spawnPos);
            int strucZ = staticGenInfo.getPositionZ(spawnPos);

            StructureGenerator.randomInstantly(world, random, structureInfo, staticGenInfo.ySelector, strucX, strucZ, false, structureName);
        }
    }

    public static void generateRandomStructuresInChunk(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider, BiomeGenBase biomeGen)
    {
        StructureSelector structureSelector = StructureRegistry.INSTANCE.getStructureSelector(biomeGen, world.provider);

        List<Pair<StructureInfo, NaturalGenerationInfo>> generated = structureSelector.generatedStructures(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);

        for (Pair<StructureInfo, NaturalGenerationInfo> pair : generated)
            generateStructureInChunk(random, chunkX, chunkZ, world, pair);
    }

    public static void generateRandomStructureInChunk(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider, BiomeGenBase biomeGen)
    {
        StructureSelector structureSelector = StructureRegistry.INSTANCE.getStructureSelector(biomeGen, world.provider);

        Pair<StructureInfo, NaturalGenerationInfo> pair = structureSelector.selectOne(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
        generateStructureInChunk(random, chunkX, chunkZ, world, pair);
    }

    protected static void generateStructureInChunk(Random random, int chunkX, int chunkZ, World world, Pair<StructureInfo, NaturalGenerationInfo> pair)
    {
        StructureInfo structureInfo = pair.getLeft();
        NaturalGenerationInfo naturalGenInfo = pair.getRight();
        String structureName = StructureRegistry.INSTANCE.structureID(structureInfo);

        int genX = chunkX * 16 + random.nextInt(16);
        int genZ = chunkZ * 16 + random.nextInt(16);

        if (!naturalGenInfo.hasLimitations() || naturalGenInfo.getLimitations().areResolved(world, structureName))
            StructureGenerator.randomInstantly(world, random, structureInfo, naturalGenInfo.ySelector, genX, genZ, true, structureName);
    }

    public static void generatePartialStructuresInChunk(Random random, final int chunkX, final int chunkZ, final World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
    {
        StructureGenerationData data = StructureGenerationData.get(world);

        for (StructureGenerationData.Entry entry : data.getEntriesAt(new ChunkCoordIntPair(chunkX, chunkZ), true))
        {
            StructureInfo structureInfo = StructureRegistry.INSTANCE.getStructure(entry.getStructureID());

            if (structureInfo != null)
            {
                StructureGenerator.partially(structureInfo, world, random, entry.lowerCoord, entry.transform, StructureInfos.chunkBoundingBox(chunkX, chunkZ), 0, entry.getStructureID(), entry.instanceData, entry.firstTime);

                if (entry.firstTime)
                {
                    entry.firstTime = false;
                    data.markDirty();
                }
            }
        }
    }
}
