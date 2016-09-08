/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures;

import ivorius.reccomplex.worldgen.StructureGenerator;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 * Created by lukas on 19.01.15.
 */
public class StructureSpawnContext
{
    @Nonnull
    public final WorldServer world;
    @Nonnull
    public final Biome biome;
    @Nonnull
    public final Random random;

    @Nonnull
    public final AxisAlignedTransform2D transform;
    @Nonnull
    public final StructureBoundingBox boundingBox;
    @Nullable
    public final StructureBoundingBox generationBB;

    public final int generationLayer;

    public final boolean generateAsSource;
    public final boolean isFirstTime;

    public StructureSpawnContext(@Nonnull WorldServer world, Biome biome, @Nonnull Random random, @Nonnull AxisAlignedTransform2D transform, @Nonnull StructureBoundingBox boundingBox, @Nullable StructureBoundingBox generationBB, int generationLayer, boolean generateAsSource, boolean isFirstTime)
    {
        this.world = world;
        this.biome = biome;
        this.random = random;
        this.transform = transform;
        this.boundingBox = boundingBox;
        this.generationBB = generationBB;
        this.generationLayer = generationLayer;
        this.generateAsSource = generateAsSource;
        this.isFirstTime = isFirstTime;
    }

    public static StructureSpawnContext complete(@Nonnull WorldServer world, @Nonnull Random random, @Nonnull AxisAlignedTransform2D transform, @Nonnull StructureBoundingBox boundingBox, int generationLayer, boolean generateAsSource)
    {
        return new StructureSpawnContext(world, StructureGenerator.getBiome(world, boundingBox), random, transform, boundingBox, null, generationLayer, generateAsSource, true);
    }

    public static StructureSpawnContext complete(@Nonnull WorldServer world, @Nonnull Random random, @Nonnull AxisAlignedTransform2D transform, BlockPos coord, StructureInfo structureInfo, int generationLayer, boolean generateAsSource)
    {
        StructureBoundingBox boundingBox = StructureInfos.structureBoundingBox(coord, StructureInfos.structureSize(structureInfo, transform));
        return new StructureSpawnContext(world, StructureGenerator.getBiome(world, boundingBox), random, transform, boundingBox, null, generationLayer, generateAsSource, true);
    }

    public static StructureSpawnContext partial(@Nonnull WorldServer world, @Nonnull Random random, @Nonnull AxisAlignedTransform2D transform, @Nonnull StructureBoundingBox boundingBox, StructureBoundingBox generationBB, int generationLayer, boolean generateAsSource, boolean isFirstTime)
    {
        return new StructureSpawnContext(world, StructureGenerator.getBiome(world, boundingBox), random, transform, boundingBox, generationBB, generationLayer, generateAsSource, isFirstTime);
    }

    public static StructureSpawnContext partial(@Nonnull WorldServer world, @Nonnull Random random, @Nonnull AxisAlignedTransform2D transform, BlockPos coord, StructureInfo structureInfo, @Nonnull StructureBoundingBox generationBB, int generationLayer, boolean generateAsSource, boolean isFirstTime)
    {
        StructureBoundingBox boundingBox = StructureInfos.structureBoundingBox(coord, StructureInfos.structureSize(structureInfo, transform));
        return new StructureSpawnContext(world, StructureGenerator.getBiome(world, boundingBox), random, transform, boundingBox, generationBB, generationLayer, generateAsSource, isFirstTime);
    }

    public boolean includes(BlockPos coord)
    {
        return generationBB == null || generationBB.isVecInside(coord);
    }

    public boolean includes(double x, double y, double z)
    {
        return generationBB == null || (x >= generationBB.minX && x <= generationBB.maxX && z >= generationBB.minZ && z <= generationBB.maxZ && y >= generationBB.minY && y <= generationBB.maxY);
    }

    public int[] boundingBoxSize()
    {
        return new int[]{boundingBox.getXSize(), boundingBox.getYSize(), boundingBox.getZSize()};
    }

    public BlockPos lowerCoord()
    {
        return new BlockPos(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
    }

    public boolean setBlock(BlockPos coord, IBlockState state, int flag)
    {
        if (includes(coord))
        {
            world.setBlockState(coord, state, flag);
            return true;
        }

        return false; // world.setBlock returns false on 'no change'
    }
}
