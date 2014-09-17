/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.blockTransformers;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.tools.IvWorldData;
import net.minecraft.block.Block;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public interface BlockTransformer
{
    boolean skipGeneration(Block block, int metadata);

    void transform(World world, Random random, Phase phase, BlockCoord origin, int[] size, AxisAlignedTransform2D transform, IvWorldData worldData);

    String displayString();

    boolean generatesInPhase(Phase phase);

    public static enum Phase
    {
        BEFORE,
        AFTER
    }
}
