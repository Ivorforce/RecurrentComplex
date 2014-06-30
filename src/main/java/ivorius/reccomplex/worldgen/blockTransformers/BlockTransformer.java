/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.blockTransformers;

import ivorius.ivtoolkit.tools.IvWorldData;
import net.minecraft.block.Block;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public interface BlockTransformer
{
    boolean matches(Block block, int metadata);

    void apply(World world, Random random, boolean beforeGeneration, int x, int y, int z, Block sourceBlock, int sourceMetadata, IvWorldData worldData);

    String displayString();

    boolean generatesBefore();

    boolean generatesAfter();
}
