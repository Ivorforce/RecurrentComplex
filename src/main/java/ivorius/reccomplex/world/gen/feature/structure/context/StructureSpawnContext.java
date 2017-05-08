/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.context;

import ivorius.reccomplex.world.gen.feature.structure.Environment;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Created by lukas on 19.01.15.
 */
public class StructureSpawnContext extends StructureLiveContext
{
    @Nonnull
    public final Random random;

    @Nullable
    public final StructureBoundingBox generationBB;
    @Nullable
    public final Predicate<Vec3i> generationPredicate;

    public final int generationLayer;

    public final GenerateMaturity generateMaturity;

    public StructureSpawnContext(@Nonnull Environment environment, @Nonnull Random random, @Nonnull AxisAlignedTransform2D transform, @Nonnull StructureBoundingBox boundingBox, @Nullable StructureBoundingBox generationBB, Predicate<Vec3i> generationPredicate, int generationLayer, boolean generateAsSource, GenerateMaturity generateMaturity)
    {
        super(transform, boundingBox, generateAsSource, environment);
        this.random = random;
        this.generationBB = generationBB;
        this.generationPredicate = generationPredicate;
        this.generationLayer = generationLayer;
        this.generateMaturity = generateMaturity;
    }

    public boolean includes(Vec3i coord)
    {
        return (generationBB == null || generationBB.isVecInside(coord))
                && (generationPredicate == null || generationPredicate.test(coord));
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

    public enum GenerateMaturity
    {
        PLAN, SUGGEST, FIRST, COMPLEMENT;

        public boolean isSuggest()
        {
            return this == PLAN || this == SUGGEST;
        }

        public boolean isFirstTime()
        {
            return this != GenerateMaturity.COMPLEMENT;
        }

        public boolean generates()
        {
            return this != PLAN;
        }
    }
}
