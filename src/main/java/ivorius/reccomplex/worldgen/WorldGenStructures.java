/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen;

import cpw.mods.fml.common.IWorldGenerator;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.events.RCEventBus;
import ivorius.reccomplex.events.StructureGenerationEvent;
import ivorius.reccomplex.events.StructureGenerationEventLite;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 24.05.14.
 */
public class WorldGenStructures implements IWorldGenerator
{

    public static final int MIN_DIST_TO_LIMIT = 3;

    public static int generateStructureRandomly(World world, Random random, StructureInfo info, int x, int z, boolean suggest)
    {
        AxisAlignedTransform2D transform = AxisAlignedTransform2D.transform(info.isRotatable() ? random.nextInt(4) : 0, info.isMirrorable() && random.nextBoolean());

        int[] size = structureSize(info, transform);

        int genX = x - size[0] / 2;
        int genZ = z - size[2] / 2;
        int genY = info.generationY(world, random, x, z);
        BlockCoord coord = new BlockCoord(genX, genY, genZ);

        generateStructureWithNotifications(info, world, random, coord, transform, 0, suggest);

        return genY;
    }

    public static void generateStructureWithNotifications(StructureInfo structureInfo, World world, Random random, BlockCoord coord, AxisAlignedTransform2D strucTransform, int layer, boolean suggest)
    {
        int[] size = structureSize(structureInfo, strucTransform);
        int[] coordInts = new int[]{coord.x, coord.y, coord.z};

        StructureSpawnContext structureSpawnContext = new StructureSpawnContext(world, random, structureBoundingBox(coord, size), layer, false, strucTransform);
        String structureName = StructureRegistry.getName(structureInfo);

        if (!suggest || (
                coord.y > MIN_DIST_TO_LIMIT && coord.y + size[1] < world.getHeight() - MIN_DIST_TO_LIMIT
                && !RCEventBus.INSTANCE.post(new StructureGenerationEvent.Suggest(structureInfo, structureSpawnContext))
                && !MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Suggest(world, structureName, coordInts, size, layer))
                ))
        {
            RCEventBus.INSTANCE.post(new StructureGenerationEvent.Pre(structureInfo, structureSpawnContext));
            MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Pre(world, structureName, coordInts, size, layer));

            structureInfo.generate(structureSpawnContext);
            RecurrentComplex.logger.trace("Generated structure '" + StructureRegistry.getName(structureInfo) + "' in " + structureSpawnContext.boundingBox);

            RCEventBus.INSTANCE.post(new StructureGenerationEvent.Post(structureInfo, structureSpawnContext));
            MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Post(world, structureName, coordInts, size, layer));
        }
        else
            RecurrentComplex.logger.trace("Canceled structure '" + StructureRegistry.getName(structureInfo) + "' generation in " + structureSpawnContext.boundingBox);
    }

    public static void generateSpawnStructure(Random random, ChunkCoordinates spawn, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
    {
        if (RCConfig.spawnStructure != null && RCConfig.spawnStructure.trim().length() > 0)
        {
            RecurrentComplex.logger.trace(String.format("Attempting to generate spawn structure"));

            StructureInfo structureInfo = StructureRegistry.getStructure(RCConfig.spawnStructure);
            if (structureInfo != null)
            {
                AxisAlignedTransform2D transform = AxisAlignedTransform2D.transform(structureInfo.isRotatable() ? random.nextInt(4) : 0, structureInfo.isMirrorable() && random.nextBoolean());
                int[] strucBB = structureSize(structureInfo, transform);

                int strucX = spawn.posX + RCConfig.spawnStructureShiftX;
                int strucZ = spawn.posZ + RCConfig.spawnStructureShiftZ;
                int strucY = structureInfo.generationY(world, random, strucX, strucZ);

                BlockCoord genCoord = new BlockCoord(strucX - strucBB[0] / 2, strucY, strucZ - strucBB[2] / 2);

                generateStructureWithNotifications(structureInfo, world, random, genCoord, transform, 0, false);
            }
            else
                RecurrentComplex.logger.warn("Could not find spawn structure '" + RCConfig.spawnStructure + "'");
        }
    }

    public static int[] structureSize(StructureInfo info, AxisAlignedTransform2D transform)
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

    public static StructureBoundingBox structureBoundingBox(BlockCoord coord, int[] size)
    {
        return new StructureBoundingBox(coord.x, coord.y, coord.z, coord.x + size[0], coord.y + size[1], coord.z + size[2]);
    }

    public static boolean chunkContains(int chunkX, int chunkZ, ChunkCoordinates coords)
    {
        return (coords.posX >> 4) == chunkX && (coords.posZ >> 4) == chunkZ;
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
    {
        boolean mayGenerate = world.getWorldInfo().isMapFeaturesEnabled();
        if (world.provider.dimensionId == 0)
        {
            ChunkCoordinates spawnPos = world.getSpawnPoint();

            if (chunkContains(chunkX, chunkZ, spawnPos))
            {
                RecurrentComplex.logger.trace(String.format("Found spawn chunk at x = %d, z = %d", chunkX << 4, chunkZ << 4));
                generateSpawnStructure(random, spawnPos, world, chunkGenerator, chunkProvider);
            }

            double distToSpawn = IvVecMathHelper.distanceSQ(new double[]{chunkX * 16 + 8, chunkZ * 16 + 8}, new double[]{spawnPos.posX, spawnPos.posZ});
            mayGenerate &= distToSpawn >= RCConfig.minDistToSpawnForGeneration * RCConfig.minDistToSpawnForGeneration;
        }

        if (mayGenerate)
        {
            BiomeGenBase biomeGen = world.getBiomeGenForCoords(chunkX * 16, chunkZ * 16);

            StructureSelector structureSelector = StructureRegistry.getStructureSelector(biomeGen, world.provider.dimensionId);
            List<StructureInfo> generated = structureSelector.generatedStructures(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);

            for (StructureInfo info : generated)
            {
                int genX = chunkX * 16 + random.nextInt(16);
                int genZ = chunkZ * 16 + random.nextInt(16);
                generateStructureRandomly(world, random, info, genX, genZ, true);

//                RecurrentComplex.logger.info("Generated " + info + " at " + genX + ", " + genY + ", " + genZ);
            }
        }
    }
}
