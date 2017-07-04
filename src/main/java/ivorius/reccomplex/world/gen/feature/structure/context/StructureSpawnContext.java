/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.context;

import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.world.chunk.gen.StructureBoundingBoxes;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.utils.RCAxisAlignedTransform;
import ivorius.reccomplex.utils.RCBlockAreas;
import ivorius.reccomplex.utils.RCStructureBoundingBoxes;
import ivorius.reccomplex.world.gen.feature.HeightMapFreezer;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
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

    private HeightMapFreezer heightMapFreezer;

    public StructureSpawnContext(@Nonnull Environment environment, @Nonnull Random random, @Nonnull AxisAlignedTransform2D transform, @Nonnull StructureBoundingBox boundingBox, @Nullable StructureBoundingBox generationBB, Predicate<Vec3i> generationPredicate, int generationLayer, boolean generateAsSource, GenerateMaturity generateMaturity)
    {
        super(transform, boundingBox, generateAsSource, environment);
        this.random = random;
        this.generationBB = generationBB;
        this.generationPredicate = generationPredicate;
        this.generationLayer = generationLayer;
        this.generateMaturity = generateMaturity;
    }

    @Nullable
    public StructureBoundingBox sourceIntersection(StructureBoundingBox area)
    {
        return this.generationBB != null
                ? RCStructureBoundingBoxes.intersection(area, BlockAreas.toBoundingBox(
                RCAxisAlignedTransform.apply(RCAxisAlignedTransform.invert(this.transform),
                        RCBlockAreas.sub(RCBlockAreas.from(this.generationBB), StructureBoundingBoxes.min(this.boundingBox)),
                        StructureBoundingBoxes.size(boundingBox), 1)))
                : area;
    }

    public StructureBoundingBox intersection(StructureBoundingBox area)
    {
        return this.generationBB != null
                ? RCStructureBoundingBoxes.intersection(area, generationBB)
                : area;
    }

    public boolean includes(Vec3i coord)
    {
        return (generationBB == null || generationBB.isVecInside(coord))
                && (generationPredicate == null || generationPredicate.test(coord));
    }

    /**
     * If the inclusion by generationBB is already given
     */
    public boolean includesComplex(Vec3i coord)
    {
        return generationPredicate == null || generationPredicate.test(coord);
    }

    public boolean setBlock(BlockPos pos, IBlockState state, int flag)
    {
        if (!RecurrentComplex.specialRegistry.isSafe(state.getBlock()))
        {
            environment.world.setBlockState(pos.toImmutable(), Blocks.AIR.getDefaultState(), 2);
            return false;
        }

        if (includes(pos))
        {
            // world.setBlock returns false on 'no change'
            environment.world.setBlockState(pos.toImmutable(), state, flag);
            if (heightMapFreezer != null) heightMapFreezer.markBlock(pos, state);
            return true;
        }

        return false;
    }

    public void freezeHeightMap(StructureBoundingBox boundingBox)
    {
        heightMapFreezer = HeightMapFreezer.freeze(boundingBox, environment.world);
    }

    public void meltHeightMap()
    {
        heightMapFreezer.melt();
        heightMapFreezer = null;
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
