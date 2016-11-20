/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.generic.placement;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Created by lukas on 30.03.15.
 */
public class StructurePlaceContext
{
    @Nonnull
    public final Random random;
    @Nonnull
    public final Environment environment;
    @Nonnull
    public final AxisAlignedTransform2D transform;
    @Nonnull
    public final StructureBoundingBox boundingBox;

    public StructurePlaceContext(@Nonnull Random random, Environment environment, @Nonnull AxisAlignedTransform2D transform, @Nonnull StructureBoundingBox boundingBox)
    {
        this.random = random;
        this.environment = environment;
        this.transform = transform;
        this.boundingBox = boundingBox;
    }
}
