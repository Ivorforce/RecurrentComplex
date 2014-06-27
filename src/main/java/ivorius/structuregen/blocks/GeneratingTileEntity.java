/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.blocks;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by lukas on 06.06.14.
 */
public interface GeneratingTileEntity
{
    void generate(World world, Random random, AxisAlignedTransform2D transform, int layer);
}
