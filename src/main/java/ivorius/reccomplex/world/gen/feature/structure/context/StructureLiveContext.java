/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.context;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 20.12.16.
 */
public class StructureLiveContext extends StructureContext
{
    @Nonnull
    public Environment environment;

    public StructureLiveContext(@Nonnull AxisAlignedTransform2D transform, @Nonnull StructureBoundingBox boundingBox, boolean generateAsSource, @Nonnull Environment environment)
    {
        super(transform, boundingBox, generateAsSource);
        this.environment = environment;
    }
}
