/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.worldgen.blockTransformers;

import ivorius.structuregen.ivtoolkit.math.IvVecMathHelper;
import ivorius.structuregen.ivtoolkit.tools.IvWorldData;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class BlockTransformerNatural implements BlockTransformer
{
    public static final double NATURAL_EXPANSION_DISTANCE = 4.0;
    public static final double NATURAL_DISTANCE_RANDOMIZATION = 6.0;

    public Block sourceBlock;
    public int sourceMetadata;

    public BlockTransformerNatural(Block sourceBlock, int sourceMetadata)
    {
        this.sourceBlock = sourceBlock;
        this.sourceMetadata = sourceMetadata;
    }

    @Override
    public boolean matches(Block block, int metadata)
    {
        return block == sourceBlock && (metadata < 0 || metadata == sourceMetadata);
    }

    @Override
    public void apply(World world, Random random, int x, int y, int z, Block sourceBlock, int sourceMetadata, IvWorldData worldData)
    {
        BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
        Block topBlock = biome.topBlock;
        Block fillerBlock = biome.fillerBlock;
        Block mainBlock = world.provider.dimensionId == -1 ? Blocks.netherrack : (world.provider.dimensionId == 1 ? Blocks.end_stone : Blocks.stone);

        int currentY = y;
        List<int[]> currentList = new ArrayList<>();
        List<int[]> nextList = new ArrayList<>();
        nextList.add(new int[]{x, z});

        while (nextList.size() > 0 && currentY > 1)
        {
            List<int[]> cachedList = currentList;
            currentList = nextList;
            nextList = cachedList;

            while (currentList.size() > 0)
            {
                int[] currentPos = currentList.remove(0);
                int currentX = currentPos[0];
                int currentZ = currentPos[1];
                Block curBlock = world.getBlock(currentX, currentY, currentZ);

                boolean replaceable = currentY == y || curBlock.isReplaceable(world, currentX, currentY, currentZ);
                if (replaceable)
                {
                    Block setBlock = hasBlockAbove(world, currentX, currentY, currentZ, mainBlock) ? mainBlock : (isTopBlock(world, currentX, currentY, currentZ) ? topBlock : fillerBlock);
                    world.setBlock(currentX, currentY, currentZ, setBlock);
                }

                if (replaceable || curBlock == topBlock || curBlock == fillerBlock || curBlock == mainBlock)
                {
                    double yForDistance = y * 0.3 + currentY * 0.7;
                    double distToOrigSQ = IvVecMathHelper.distanceSQ(new double[]{x, y, z}, new double[]{currentX, yForDistance, currentZ});
                    double add = (random.nextDouble() - random.nextDouble()) * NATURAL_DISTANCE_RANDOMIZATION;
                    distToOrigSQ += add < 0 ? -(add * add) : (add * add);

                    if (distToOrigSQ < NATURAL_EXPANSION_DISTANCE * NATURAL_EXPANSION_DISTANCE)
                    {
                        addIfNew(nextList, currentX, currentZ);
                        addIfNew(nextList, currentX - 1, currentZ);
                        addIfNew(nextList, currentX + 1, currentZ);
                        addIfNew(nextList, currentX, currentZ - 1);
                        addIfNew(nextList, currentX, currentZ + 1);
                    }
                }
            }

            currentY--;
        }
    }

    private void addIfNew(List<int[]> list, int... object)
    {
        if (!list.contains(object))
        {
            list.add(object);
        }
    }

    private boolean hasBlockAbove(World world, int x, int y, int z, Block blockType)
    {
        for (; y < world.getHeight(); y++)
        {
            if (world.getBlock(x, y, z) == blockType)
                return true;
        }

        return false;
    }

    private boolean isTopBlock(World world, int x, int y, int z)
    {
        return !world.isBlockNormalCubeDefault(x, y + 1, z, false);
    }

    @Override
    public String displayString()
    {
        return "Natural: " + sourceBlock.getLocalizedName();
    }

    @Override
    public boolean generatesBefore()
    {
        return true;
    }
}
