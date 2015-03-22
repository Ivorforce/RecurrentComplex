/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import net.minecraft.world.gen.structure.StructureBoundingBox;

/**
 * Created by lukas on 22.02.15.
 */
public class StructureInfos
{
    public static StructureBoundingBox structureBoundingBox(BlockCoord coord, int[] size)
    {
        return new StructureBoundingBox(coord.x, coord.y, coord.z, coord.x + size[0], coord.y + size[1], coord.z + size[2]);
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

    public static StructureBoundingBox chunkBoundingBox(int chunkX, int chunkZ)
    {
        return new StructureBoundingBox(chunkX << 4, chunkZ << 4, chunkX << 4 + 15, chunkZ << 4 + 15);
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
}
