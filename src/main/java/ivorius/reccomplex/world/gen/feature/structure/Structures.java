/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure;

import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 22.02.15.
 */
public class Structures
{
    public static StructureBoundingBox structureBoundingBox(@Nonnull BlockPos coord, @Nonnull int[] size)
    {
        return new StructureBoundingBox(coord, coord.add(new Vec3i(size[0], size[1], size[2])));
    }

    public static int[] structureSize(@Nonnull Structure info, @Nonnull AxisAlignedTransform2D transform)
    {
        return structureSize(info.size(), transform);
    }

    public static int[] structureSize(int[] size, AxisAlignedTransform2D transform)
    {
        if (transform.getRotation() % 2 == 1)
        {
            int cache = size[0];
            size[0] = size[2];
            size[2] = cache;
        }
        return size;
    }

    public static StructureBoundingBox chunkBoundingBox(ChunkPos chunkPos)
    {
        return new StructureBoundingBox(chunkPos.chunkXPos << 4, chunkPos.chunkZPos << 4, (chunkPos.chunkXPos << 4) + 15, (chunkPos.chunkZPos << 4) + 15);
    }

    public static StructureBoundingBox intersection(StructureBoundingBox bb1, StructureBoundingBox bb2)
    {
        int x1 = Math.max(bb1.minX, bb2.minX);
        int y1 = Math.max(bb1.minY, bb2.minY);
        int z1 = Math.max(bb1.minZ, bb2.minZ);
        int x2 = Math.min(bb1.maxX, bb2.maxX);
        int y2 = Math.min(bb1.maxY, bb2.maxY);
        int z2 = Math.min(bb1.maxZ, bb2.maxZ);

        return new StructureBoundingBox(
                Math.min(x1, x2),
                Math.min(y1, y2),
                Math.min(z1, z2),
                Math.max(x1, x2),
                Math.max(y1, y2),
                Math.max(z1, z2)
        );
    }

    public static GuiValidityStateIndicator.State isSimpleIDState(String id)
    {
        return isSimpleID(id)
                ? GuiValidityStateIndicator.State.VALID
                : GuiValidityStateIndicator.State.INVALID;
    }

    public static boolean isSimpleID(String id)
    {
        return !id.trim().isEmpty() && id.chars().allMatch(Character::isJavaIdentifierPart);
    }
}
