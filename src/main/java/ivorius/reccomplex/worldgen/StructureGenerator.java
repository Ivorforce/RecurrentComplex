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
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.StructureInfos;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.GenerationYSelector;
import ivorius.reccomplex.structures.generic.gentypes.NaturalGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.StaticGenerationInfo;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 24.05.14.
 */
public class StructureGenerator
{
    public static final int MIN_DIST_TO_LIMIT = 1;

    public static int generateStructureRandomly(World world, Random random, StructureInfo info, @Nullable GenerationYSelector ySelector, int x, int z, boolean suggest, String structureName)
    {
        AxisAlignedTransform2D transform = AxisAlignedTransform2D.transform(info.isRotatable() ? random.nextInt(4) : 0, info.isMirrorable() && random.nextBoolean());

        int[] size = StructureInfos.structureSize(info, transform);

        int genX = x - size[0] / 2;
        int genZ = z - size[2] / 2;
        int genY = ySelector != null ? ySelector.generationY(world, random, StructureInfos.structureBoundingBox(new BlockCoord(genX, 0, genZ), size)) : world.getHeightValue(x, z);
        BlockCoord coord = new BlockCoord(genX, genY, genZ);

        generateStructureWithNotifications(info, world, random, coord, transform, 0, suggest, structureName);

        return genY;
    }

    public static boolean generatePartialStructure(StructureInfo structureInfo, World world, Random random, BlockCoord coord, AxisAlignedTransform2D transform, @Nullable StructureBoundingBox boundingBox, int layer, String structureName, boolean firstTime)
    {
        StructureBoundingBox structureBoundingBox = StructureInfos.structureBoundingBox(coord, StructureInfos.structureSize(structureInfo, transform));
        StructureSpawnContext structureSpawnContext = new StructureSpawnContext(world, random, structureBoundingBox, boundingBox, layer, false, transform, firstTime);
        structureInfo.generate(structureSpawnContext);

        if (firstTime)
            RecurrentComplex.logger.trace(String.format("Generated structure '%s' in %s", name(structureName), structureSpawnContext.boundingBox));

        if (structureName != null && firstTime)
            StructureGenerationData.get(world).addNewEntry(structureName, coord, transform);

        return true;
    }

    public static boolean generateStructureWithNotifications(StructureInfo structureInfo, World world, Random random, BlockCoord coord, AxisAlignedTransform2D transform, int layer, boolean suggest, String structureName)
    {
        int[] size = StructureInfos.structureSize(structureInfo, transform);
        int[] coordInts = new int[]{coord.x, coord.y, coord.z};

        StructureSpawnContext structureSpawnContext = new StructureSpawnContext(world, random, StructureInfos.structureBoundingBox(coord, size), layer, false, transform);

        if (!suggest || (
                coord.y >= MIN_DIST_TO_LIMIT && coord.y + size[1] <= world.getHeight() - 1 - MIN_DIST_TO_LIMIT
                        && (!RCConfig.avoidOverlappingGeneration || StructureGenerationData.get(world).getEntries(structureSpawnContext.boundingBox).size() == 0)
                        && !RCEventBus.INSTANCE.post(new StructureGenerationEvent.Suggest(structureInfo, structureSpawnContext))
                        && !MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Suggest(world, structureName, coordInts, size, layer))
        ))
        {
            RCEventBus.INSTANCE.post(new StructureGenerationEvent.Pre(structureInfo, structureSpawnContext));
            MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Pre(world, structureName, coordInts, size, layer));

            structureInfo.generate(structureSpawnContext);

            RecurrentComplex.logger.trace(String.format("Generated structure '%s' in %s", name(structureName), structureSpawnContext.boundingBox));

            RCEventBus.INSTANCE.post(new StructureGenerationEvent.Post(structureInfo, structureSpawnContext));
            MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Post(world, structureName, coordInts, size, layer));

            if (structureName != null)
                StructureGenerationData.get(world).addNewEntry(structureName, coord, transform);

            return true;
        }
        else
            RecurrentComplex.logger.trace(String.format("Canceled structure '%s' generation in %s", structureName, structureSpawnContext.boundingBox));

        return false;
    }

    private static String name(String structureName)
    {
        return structureName != null ? structureName : "Unknown";
    }
}
