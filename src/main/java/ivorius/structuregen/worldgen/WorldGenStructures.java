package ivorius.structuregen.worldgen;

import cpw.mods.fml.common.IWorldGenerator;
import ivorius.structuregen.StructureGen;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 24.05.14.
 */
public class WorldGenStructures implements IWorldGenerator
{
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
    {
        if (world.getWorldInfo().isMapFeaturesEnabled())
        {
            BiomeGenBase biomeGen = world.getBiomeGenForCoords(chunkX * 16 + 16, chunkZ * 16 + 16);

            StructureSelector structureSelector = StructureHandler.getStructureSelectorInBiome(biomeGen);
            List<StructureInfo> generated = structureSelector.generatedStructures(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);

            for (StructureInfo info : generated)
            {
                int genX = chunkX * 16 + random.nextInt(16);
                int genZ = chunkZ * 16 + random.nextInt(16);
                int genY = info.generationY(world, random, genX, genZ);

                info.generate(world, random, genX, genY, genZ, true, 0);
                StructureGen.logger.info("Generated " + info + " at " + genX + ", " + genY + ", " + genZ);
            }
        }
    }
}
