/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure;

import ivorius.reccomplex.utils.StructureBoundingBoxes;
import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Created by lukas on 30.03.15.
 */
public class StructurePrepareContext
{
    @Nonnull
    public final Random random;
    @Nonnull
    public final Environment environment;
    @Nonnull
    public final AxisAlignedTransform2D transform;
    @Nonnull
    public final StructureBoundingBox boundingBox;

    public final boolean generateAsSource;

    public StructurePrepareContext(@Nonnull Random random, @Nonnull Environment environment, @Nonnull AxisAlignedTransform2D transform, @Nonnull StructureBoundingBox boundingBox, boolean generateAsSource)
    {
        this.random = random;
        this.environment = environment;
        this.transform = transform;
        this.boundingBox = boundingBox;
        this.generateAsSource = generateAsSource;
    }
}
