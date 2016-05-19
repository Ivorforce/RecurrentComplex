/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen;

import net.minecraft.util.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.events.RCEventBus;
import ivorius.reccomplex.events.StructureGenerationEvent;
import ivorius.reccomplex.events.StructureGenerationEventLite;
import ivorius.reccomplex.structures.*;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Created by lukas on 24.05.14.
 */
public class StructureGenerator
{
    public static final int MIN_DIST_TO_LIMIT = 1;

    public static <I extends NBTStorable> void partially(StructureInfo<I> structureInfo, World world, Random random, BlockPos coord, AxisAlignedTransform2D transform, @Nullable StructureBoundingBox generationBB, int layer, String structureID, NBTTagCompound instanceData, boolean firstTime)
    {
        partially(structureInfo, world, random, coord, transform, generationBB, layer, structureID,
                structureInfo.loadInstanceData(new StructureLoadContext(transform, StructureInfos.structureBoundingBox(coord, StructureInfos.structureSize(structureInfo, transform)), false), instanceData),
                firstTime);
    }

    public static <I extends NBTStorable> void partially(StructureInfo<I> structureInfo, World world, Random random, BlockPos coord, AxisAlignedTransform2D transform, @Nullable StructureBoundingBox generationBB, int layer, String structureID, I instanceData, boolean firstTime)
    {
        StructureSpawnContext structureSpawnContext = StructureSpawnContext.partial(world, random, transform, coord, structureInfo, generationBB, layer, false, firstTime);
        int[] coordInts = coordInts(structureSpawnContext.boundingBox);
        int[] sizeInts = sizeInts(structureSpawnContext.boundingBox);

        if (firstTime)
        {
            RCEventBus.INSTANCE.post(new StructureGenerationEvent.Pre(structureInfo, structureSpawnContext));
            MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Pre(world, structureID, coordInts, sizeInts, layer));
        }

        structureInfo.generate(structureSpawnContext, instanceData);

        if (firstTime)
        {
            RecurrentComplex.logger.trace(String.format("Generated structure '%s' in %s", name(structureID), structureSpawnContext.boundingBox));

            RCEventBus.INSTANCE.post(new StructureGenerationEvent.Post(structureInfo, structureSpawnContext));
            MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Post(world, structureID, coordInts, sizeInts, layer));
        }
    }

    public static <I extends NBTStorable> void directly(StructureInfo<I> structureInfo, StructureSpawnContext context)
    {
        structureInfo.generate(context, structureInfo.prepareInstanceData(new StructurePrepareContext(context.random, context.transform, context.boundingBox, context.generateAsSource)));
    }

    public static int randomInstantly(World world, Random random, StructureInfo info, @Nullable YSelector ySelector, int x, int z, boolean suggest, String structureName)
    {
        AxisAlignedTransform2D transform = AxisAlignedTransform2D.from(info.isRotatable() ? random.nextInt(4) : 0, info.isMirrorable() && random.nextBoolean());

        int[] size = StructureInfos.structureSize(info, transform);

        int genX = x - size[0] / 2;
        int genZ = z - size[2] / 2;
        int genY = ySelector != null ? ySelector.selectY(world, random, StructureInfos.structureBoundingBox(new BlockPos(genX, 0, genZ), size)) : world.getHeight(new BlockPos(x, 0, z)).getY();
        BlockPos coord = new BlockPos(genX, genY, genZ);

        instantly(info, world, random, coord, transform, 0, suggest, structureName, false);

        return genY;
    }

    public static <I extends NBTStorable> boolean instantly(StructureInfo<I> structureInfo, World world, Random random, BlockPos coord, AxisAlignedTransform2D transform, int layer, boolean suggest, String structureID, boolean asSource)
    {
        StructureSpawnContext structureSpawnContext = StructureSpawnContext.complete(world, random, transform, coord, structureInfo, layer, asSource);
        int[] size = sizeInts(structureSpawnContext.boundingBox);
        int[] coordInts = coordInts(structureSpawnContext.boundingBox);

        if (!suggest || (
                coord.getY() >= MIN_DIST_TO_LIMIT && coord.getY() + size[1] <= world.getHeight() - 1 - MIN_DIST_TO_LIMIT
                        && (!RCConfig.avoidOverlappingGeneration || StructureGenerationData.get(world).getEntriesAt(structureSpawnContext.boundingBox).size() == 0)
                        && !RCEventBus.INSTANCE.post(new StructureGenerationEvent.Suggest(structureInfo, structureSpawnContext))
                        && !MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Suggest(world, structureID, coordInts, size, layer))
        ))
        {
            RCEventBus.INSTANCE.post(new StructureGenerationEvent.Pre(structureInfo, structureSpawnContext));
            MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Pre(world, structureID, coordInts, size, layer));

            structureInfo.generate(structureSpawnContext, structureInfo.prepareInstanceData(new StructurePrepareContext(random, transform, structureSpawnContext.boundingBox, structureSpawnContext.generateAsSource)));

            RecurrentComplex.logger.trace(String.format("Generated structure '%s' in %s", name(structureID), structureSpawnContext.boundingBox));

            RCEventBus.INSTANCE.post(new StructureGenerationEvent.Post(structureInfo, structureSpawnContext));
            MinecraftForge.EVENT_BUS.post(new StructureGenerationEventLite.Post(world, structureID, coordInts, size, layer));

            if (structureID != null)
                StructureGenerationData.get(world).addCompleteEntry(structureID, coord, transform);

            return true;
        }
        else
            RecurrentComplex.logger.trace(String.format("Canceled structure '%s' generation in %s", structureID, structureSpawnContext.boundingBox));

        return false;
    }

    private static String name(String structureName)
    {
        return structureName != null ? structureName : "Unknown";
    }

    private static int[] coordInts(StructureBoundingBox bb)
    {
        return new int[]{bb.minX, bb.minY, bb.minZ};
    }

    private static int[] sizeInts(StructureBoundingBox bb)
    {
        return new int[]{bb.getXSize(), bb.getYSize(), bb.getZSize()};
    }
}
