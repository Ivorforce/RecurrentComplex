/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 29.11.16.
 */
public class RCStructureBoundingBoxes
{
    @Nonnull
    public static BlockPos getCenter(StructureBoundingBox boundingBox)
    {
        return new BlockPos((boundingBox.minX + boundingBox.maxX) / 2, (boundingBox.minY + boundingBox.maxY) / 2, (boundingBox.minZ + boundingBox.maxZ) / 2);
    }
}
