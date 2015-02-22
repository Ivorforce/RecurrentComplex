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
        int[] size = info.structureBoundingBox();
        if (transform.getRotation() % 2 == 1)
        {
            int cache = size[0];
            size[0] = size[2];
            size[2] = cache;
        }
        return size;
    }
}
