/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

/**
 * Created by lukas on 18.09.16.
 */
public class WorldCache
{
    public final World world;

    private StructureBoundingBox boundingBox;
    private IBlockState[] states;

    public WorldCache(World world, StructureBoundingBox boundingBox)
    {
        this.world = world;
        this.boundingBox = boundingBox;
        states = new IBlockState[boundingBox.getXSize() * boundingBox.getYSize() * boundingBox.getZSize()];
    }

    public IBlockState getBlockState(BlockPos pos)
    {
        if (!boundingBox.isVecInside(pos))
            return world.getBlockState(pos);

        int index = ((pos.getX() - boundingBox.minX) * boundingBox.getYSize()
                + (pos.getY() - boundingBox.minY)) * boundingBox.getZSize()
                + (pos.getZ() - boundingBox.minZ);

        IBlockState state = states[index];

        return state != null ? state : (states[index] = world.getBlockState(pos));
    }
}
