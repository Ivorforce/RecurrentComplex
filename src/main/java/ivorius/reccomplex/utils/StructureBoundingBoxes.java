/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.utils;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lukas on 01.04.15.
 */
public class StructureBoundingBoxes
{
    public static Set<ChunkPos> rasterize(StructureBoundingBox boundingBox)
    {
        if (boundingBox != null)
        {
            int minX = boundingBox.minX >> 4;
            int maxX = boundingBox.maxX >> 4;

            int minZ = boundingBox.minZ >> 4;
            int maxZ = boundingBox.maxZ >> 4;

            Set<ChunkPos> pairs = new HashSet<>((maxX - minX + 1) * (maxZ - minZ + 1));
            for (int x = minX; x <= maxX; x++)
                for (int z = minZ; z <= maxZ; z++)
                    pairs.add(new ChunkPos(x, z));

            return pairs;
        }

        return Collections.emptySet();
    }

    public static boolean fitsY(StructureBoundingBox boundingBox, int minY, int maxY)
    {
        return boundingBox.minY >= minY && boundingBox.maxY < maxY;
    }
}
