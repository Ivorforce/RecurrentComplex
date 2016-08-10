/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures;

import net.minecraft.util.BlockPos;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import net.minecraft.util.Vec3i;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.gen.structure.StructureBoundingBox;

/**
 * Created by lukas on 22.02.15.
 */
public class StructureInfos
{
    public static StructureBoundingBox structureBoundingBox(BlockPos coord, int[] size)
    {
        return new StructureBoundingBox(coord, coord.add(new Vec3i(size[0], size[1], size[2])));
    }

    public static int[] structureSize(StructureInfo info, AxisAlignedTransform2D transform)
    {
        return structureSize(info.structureBoundingBox(), transform);
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

    public static BlockPos transformedLowerCoord(BlockPos coord, int[] size, AxisAlignedTransform2D transform)
    {
        // TODO Fix for mirror
        if (transform.getRotation() == 1 || transform.getRotation() == 2)
            coord = coord.subtract(new Vec3i(size[0] - 1, 0, 0));
        if (transform.getRotation() == 3 || transform.getRotation() == 2)
            coord = coord.subtract(new Vec3i(0, 0, size[2] - 1));
        return coord;
    }

    public static StructureBoundingBox chunkBoundingBox(ChunkCoordIntPair chunkPos)
    {
        return new StructureBoundingBox(chunkPos.chunkXPos << 4, chunkPos.chunkZPos << 4, chunkPos.chunkXPos << 4 + 15, chunkPos.chunkZPos << 4 + 15);
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

    public static GuiValidityStateIndicator.State defaultIDValidityState(StructureGenerationInfo genInfo)
    {
        String id = genInfo.id();
        return id.trim().isEmpty() || !id.chars().allMatch(Character::isJavaIdentifierPart)
                ? GuiValidityStateIndicator.State.INVALID
                : GuiValidityStateIndicator.State.VALID;
    }
}
