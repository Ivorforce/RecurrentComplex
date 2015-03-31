/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen;

import cpw.mods.fml.common.IWorldGenerator;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.StaticGenerationInfo;
import net.minecraft.util.ChunkCoordinates;
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
        BiomeGenBase biomeGen = world.getBiomeGenForCoords(chunkX * 16, chunkZ * 16);
        boolean mayGenerate = world.getWorldInfo().isMapFeaturesEnabled() && RCConfig.isGenerationEnabled(biomeGen.biomeName);

        final ChunkCoordinates spawnPos = world.getSpawnPoint();

        for (Pair<StructureInfo, StaticGenerationInfo> pair : StructureRegistry.getStaticStructuresAt(chunkX, chunkZ, world, spawnPos))
        {
            RecurrentComplex.logger.trace(String.format("Spawning static structure at x = %d, z = %d", chunkX << 4, chunkZ << 4));

            StaticGenerationInfo staticGenInfo = pair.getRight();
            StructureInfo structureInfo = pair.getLeft();
            String structureName = StructureRegistry.structureID(structureInfo);

            int strucX = staticGenInfo.getPositionX(spawnPos);
            int strucZ = staticGenInfo.getPositionZ(spawnPos);

            StructureGenerator.randomInstantly(world, random, structureInfo, staticGenInfo.ySelector, strucX, strucZ, false, structureName);
        }

        if (world.provider.dimensionId == 0)
        {
            double distToSpawn = IvVecMathHelper.distanceSQ(new double[]{chunkX * 16 + 8, chunkZ * 16 + 8}, new double[]{spawnPos.posX, spawnPos.posZ});
            mayGenerate &= distToSpawn >= RCConfig.minDistToSpawnForGeneration * RCConfig.minDistToSpawnForGeneration;
        }

        if (mayGenerate)
        {
            StructureSelector structureSelector = StructureRegistry.getStructureSelector(biomeGen, world.provider);
            List<Pair<StructureInfo, NaturalGenerationInfo>> generated = structureSelector.generatedStructures(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);

            for (Pair<StructureInfo, NaturalGenerationInfo> pair : generated)
            {
                StructureInfo structureInfo = pair.getLeft();
                String structureName = StructureRegistry.structureID(structureInfo);

                int genX = chunkX * 16 + random.nextInt(16);
                int genZ = chunkZ * 16 + random.nextInt(16);

                StructureGenerator.randomInstantly(world, random, structureInfo, pair.getRight().ySelector, genX, genZ, true, structureName);

//                RecurrentComplex.logger.info("Generated " + info + " at " + genX + ", " + genY + ", " + genZ);
            }
        }
    }
}
