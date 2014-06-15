package ivorius.structuregen.blocks;

import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by lukas on 06.06.14.
 */
public interface GeneratingTileEntity
{
    void generate(World world, Random random, int layer);
}
