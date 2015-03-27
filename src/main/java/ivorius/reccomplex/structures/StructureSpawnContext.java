/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
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
    public final World world;
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

    public StructureSpawnContext(@Nonnull World world, @Nonnull Random random, @Nonnull StructureBoundingBox boundingBox, int generationLayer, boolean generateAsSource, @Nonnull AxisAlignedTransform2D transform)
    {
        this.world = world;
        this.random = random;
        this.transform = transform;
        this.boundingBox = boundingBox;
        this.generationBB = null;
        this.generationLayer = generationLayer;
        this.generateAsSource = generateAsSource;
        isFirstTime = true;
    }

    public StructureSpawnContext(@Nonnull World world, @Nonnull Random random, @Nonnull StructureBoundingBox boundingBox, StructureBoundingBox generationBB, int generationLayer, boolean generateAsSource, @Nonnull AxisAlignedTransform2D transform, boolean isFirstTime)
    {
        this.world = world;
        this.random = random;
        this.transform = transform;
        this.boundingBox = boundingBox;
        this.generationBB = null;
        this.generationLayer = generationLayer;
        this.generateAsSource = generateAsSource;
        this.isFirstTime = isFirstTime;
    }

    public StructureSpawnContext(@Nonnull World world, @Nonnull Random random, BlockCoord coord, @Nonnull AxisAlignedTransform2D transform, int generationLayer, boolean generateAsSource, StructureInfo structureInfo)
    {
        this.world = world;
        this.random = random;
        boundingBox = StructureInfos.structureBoundingBox(coord, StructureInfos.structureSize(structureInfo, transform));
        generationBB = boundingBox;
        this.transform = transform;
        this.generationLayer = generationLayer;
        this.generateAsSource = generateAsSource;
        isFirstTime = true;
    }

    public boolean includes(BlockCoord coord)
    {
        return generationBB == null || generationBB.isVecInside(coord.x, coord.y, coord.z);
    }

    public boolean includes(int x, int y, int z)
    {
        return generationBB == null || generationBB.isVecInside(x, y, z);
    }

    public boolean includes(double x, double y, double z)
    {
        return generationBB == null || generationBB.isVecInside(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
    }

    public int[] boundingBoxSize()
    {
        return new int[]{boundingBox.getXSize(), boundingBox.getYSize(), boundingBox.getZSize()};
    }

    public BlockCoord lowerCoord()
    {
        return new BlockCoord(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
    }

    public boolean setBlock(BlockCoord coord, Block block, int meta)
    {
        if (includes(coord.x, coord.y, coord.z))
        {
            world.setBlock(coord.x, coord.y, coord.z, block, meta, 2);
            return true;
        }

        return false;
    }

    public boolean setBlock(int x, int y, int z, Block block, int meta)
    {
        if (includes(x, y, z))
        {
            world.setBlock(x, y, z, block, meta, 2);
            return true;
        }

        return false;
    }
}
