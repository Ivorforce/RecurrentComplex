/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure;

import ivorius.reccomplex.gui.GuiValidityStateIndicator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 22.02.15.
 */
public class Structures
{
    public static StructureBoundingBox boundingBox(@Nonnull BlockPos coord, @Nonnull int[] size)
    {
        return new StructureBoundingBox(coord, coord.add(size[0] - 1, size[1] - 1, size[2] - 1));
    }

    public static StructureBoundingBox chunkBoundingBox(ChunkPos chunkPos, boolean decorate)
    {
        int shift = decorate ? 8 : 0;
        int minZ = (chunkPos.z << 4) + shift;
        int minX = (chunkPos.x << 4) + shift;
        return new StructureBoundingBox(minX, minZ, minX + 15, minZ + 15);
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
