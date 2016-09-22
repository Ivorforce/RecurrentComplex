/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures;

import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import net.minecraft.block.state.IBlockState;
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
    public Environment environment;
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
    public final GenerateMaturity generateMaturity;

    public StructureSpawnContext(@Nonnull Environment environment, @Nonnull Random random, @Nonnull AxisAlignedTransform2D transform, @Nonnull StructureBoundingBox boundingBox, @Nullable StructureBoundingBox generationBB, int generationLayer, boolean generateAsSource, GenerateMaturity generateMaturity)
    {
        this.environment = environment;
        this.random = random;
        this.transform = transform;
        this.boundingBox = boundingBox;
        this.generationBB = generationBB;
        this.generationLayer = generationLayer;
        this.generateAsSource = generateAsSource;
        this.generateMaturity = generateMaturity;
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
            environment.world.setBlockState(coord.toImmutable(), state, flag);
            return true;
        }

        return false; // world.setBlock returns false on 'no change'
    }

    public boolean isFirstTime()
    {
        return generateMaturity != GenerateMaturity.COMPLEMENT;
    }

    public enum GenerateMaturity
    {
        SUGGEST, FIRST, COMPLEMENT
    }
}
