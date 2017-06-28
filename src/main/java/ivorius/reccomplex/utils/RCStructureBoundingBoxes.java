/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.ivtoolkit.world.chunk.gen.StructureBoundingBoxes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by lukas on 29.11.16.
 */
public class RCStructureBoundingBoxes
{
    public static Set<ChunkPos> rasterize(StructureBoundingBox boundingBox)
    {
        if (!valid(boundingBox))
            return Collections.emptySet();

        return StructureBoundingBoxes.rasterize(boundingBox);
    }

    public static boolean valid(StructureBoundingBox boundingBox)
    {
        return boundingBox != null
                && boundingBox.minX <= boundingBox.maxX
                && boundingBox.minY <= boundingBox.maxY
                && boundingBox.minZ <= boundingBox.maxZ;
    }

    @Nonnull
    public static BlockPos getCenter(StructureBoundingBox boundingBox)
    {
        return new BlockPos((boundingBox.minX + boundingBox.maxX) / 2, (boundingBox.minY + boundingBox.maxY) / 2, (boundingBox.minZ + boundingBox.maxZ) / 2);
    }

    @Nullable
    public static StructureBoundingBox intersection(StructureBoundingBox left, StructureBoundingBox right)
    {
        if (!left.intersectsWith(right))
            return null;

        return new StructureBoundingBox(
                Math.max(left.minX, right.minX), Math.max(left.minY, right.minY), Math.max(left.minZ, right.minZ),
                Math.min(left.maxX, right.maxX), Math.min(left.maxY, right.maxY), Math.min(left.maxZ, right.maxZ)
        );
    }

    @Nonnull
    public static Iterable<BlockPos> positions(@Nonnull StructureBoundingBox area)
    {
        return BlockAreas.positions(RCBlockAreas.from(area));
    }

    @Nonnull
    public static Iterable<BlockPos.MutableBlockPos> mutablePositions(@Nonnull StructureBoundingBox area)
    {
        return BlockAreas.mutablePositions(RCBlockAreas.from(area));
    }

    @Nonnull
    public static Stream<BlockPos> streamPositions(@Nonnull StructureBoundingBox area)
    {
        return BlockAreas.streamPositions(RCBlockAreas.from(area));
    }

    @Nonnull
    public static Stream<BlockPos.MutableBlockPos> streamMutablePositions(@Nonnull StructureBoundingBox area)
    {
        return BlockAreas.streamMutablePositions(RCBlockAreas.from(area));
    }
}
