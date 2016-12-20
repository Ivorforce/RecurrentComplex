/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.context;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 30.03.15.
 */
public class StructureLoadContext extends StructureContext
{
    public StructureLoadContext(@Nonnull AxisAlignedTransform2D transform, @Nonnull StructureBoundingBox boundingBox, boolean generateAsSource)
    {
        super(transform, boundingBox, generateAsSource);
    }
}
