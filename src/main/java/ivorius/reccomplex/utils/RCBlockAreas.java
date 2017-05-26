/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.utils;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockAreas;
import ivorius.ivtoolkit.blocks.BlockPositions;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nullable;

/**
 * Created by lukas on 17.05.17.
 */
public class RCBlockAreas
{
    public static BlockArea add(BlockArea area, Vec3i pos)
    {
        area.setPoint1(area.getPoint1().add(pos));
        area.setPoint2(area.getPoint2().add(pos));
        return area;
    }

    public static BlockArea sub(BlockArea area, Vec3i pos)
    {
        area.setPoint1(area.getPoint1().subtract(pos));
        area.setPoint2(area.getPoint2().subtract(pos));
        return area;
    }

    public static BlockArea from(StructureBoundingBox bb)
    {
        return new BlockArea(new BlockPos(bb.minX, bb.minY, bb.minZ), new BlockPos(bb.maxX, bb.maxY, bb.maxZ));
    }
}
