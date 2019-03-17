/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.structure.context;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.world.chunk.gen.StructureBoundingBoxes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 20.12.16.
 */
public class StructureContext
{
    @Nonnull
    public final AxisAlignedTransform2D transform;
    @Nonnull
    public final StructureBoundingBox boundingBox;

    public final boolean generateAsSource;

    public StructureContext(@Nonnull AxisAlignedTransform2D transform, @Nonnull StructureBoundingBox boundingBox, boolean generateAsSource)
    {
        this.transform = transform;
        this.boundingBox = boundingBox;
        this.generateAsSource = generateAsSource;
    }

    public BlockPos lowerCoord()
    {
        return StructureBoundingBoxes.min(boundingBox);
    }

    public int[] areaSize()
    {
        return StructureBoundingBoxes.size(boundingBox);
    }
}
