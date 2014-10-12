/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen;

import cpw.mods.fml.common.IWorldGenerator;
import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.events.RCEventBus;
import ivorius.reccomplex.events.StructureGenerationEvent;
import ivorius.reccomplex.events.StructureGenerationEventLite;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.MinecraftForge;

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
        boolean mayGenerate = world.getWorldInfo().isMapFeaturesEnabled();
        if (world.provider.dimensionId == 0)
        {
            ChunkCoordinates spawnPos = world.getSpawnPoint();

            double distToSpawn = IvVecMathHelper.distanceSQ(new double[]{chunkX * 16 + 8, chunkZ * 16 + 8}, new double[]{spawnPos.posX, spawnPos.posZ});
            mayGenerate &= distToSpawn >= RCConfig.minDistToSpawnForGeneration * RCConfig.minDistToSpawnForGeneration;
        }

        if (mayGenerate)
        {
            BiomeGenBase biomeGen = world.getBiomeGenForCoords(chunkX * 16, chunkZ * 16);

            StructureSelector structureSelector = StructureHandler.getStructureSelectorInBiome(biomeGen);
            List<StructureInfo> generated = structureSelector.generatedStructures(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);

            for (StructureInfo info : generated)
            {
                int genX = chunkX * 16 + random.nextInt(16);
                int genZ = chunkZ * 16 + random.nextInt(16);
                int genY = generateStructureRandomly(world, random, info, genX, genZ);

//                RecurrentComplex.logger.info("Generated " + info + " at " + genX + ", " + genY + ", " + genZ);
            }
        }
    }

    public static int generateStructureRandomly(World world, Random random, StructureInfo info, int x, int z)
    {
        AxisAlignedTransform2D transform = AxisAlignedTransform2D.transform(info.isRotatable() ? random.nextInt(4) : 0, info.isMirrorable() && random.nextBoolean());

        int[] size = structureBoundingBox(info, transform);

        int genX = x - size[0] / 2;
        int genZ = z - size[2] / 2;
        int genY = info.generationY(world, random, x, z);
        BlockCoord coord = new BlockCoord(genX, genY, genZ);

        generateStructureWithNotifications(info, world, random, coord, transform, 0);

        return genY;
    }

    public static void generateStructureWithNotifications(StructureInfo structureInfo, World world, Random random, BlockCoord coord, AxisAlignedTransform2D strucTransform, int layer)
    {
        int[] size = structureBoundingBox(structureInfo, strucTransform);
        BlockArea genArea = new BlockArea(coord, coord.add(size[0] - 1, size[1] - 1, size[2] - 1));
        int[] coordInts = new int[]{coord.x, coord.y, coord.z};

        RCEventBus.INSTANCE.post(new StructureGenerationEvent.Pre(world, structureInfo, strucTransform, genArea, layer));
        MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Pre(world, StructureHandler.getName(structureInfo), coordInts, size, layer));

        structureInfo.generate(world, random, coord, strucTransform, layer);

        RCEventBus.INSTANCE.post(new StructureGenerationEvent.Post(world, structureInfo, strucTransform, genArea, layer));
        MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Post(world, StructureHandler.getName(structureInfo), coordInts, size, layer));
    }

    public static int[] structureBoundingBox(StructureInfo info, AxisAlignedTransform2D transform)
    {
        int[] size = info.structureBoundingBox();
        if (transform.getRotation() % 2 == 1)
        {
            int cache = size[0];
            size[0] = size[2];
            size[2] = cache;
        }
        return size;
    }
}
