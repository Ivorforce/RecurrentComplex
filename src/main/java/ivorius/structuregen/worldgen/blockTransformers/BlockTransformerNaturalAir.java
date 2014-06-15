/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.worldgen.blockTransformers;

import ivorius.structuregen.ivtoolkit.IvMathHelper;
import ivorius.structuregen.ivtoolkit.IvWorldData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 25.05.14.
 */
public class BlockTransformerNaturalAir implements BlockTransformer
{
    public static final double NATURAL_EXPANSION_DISTANCE = 4.0;
    public static final double NATURAL_DISTANCE_RANDOMIZATION = 6.0;

    public Block sourceBlock;
    public int sourceMetadata;

    public BlockTransformerNaturalAir(Block sourceBlock, int sourceMetadata)
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

        int currentY = y;
        List<int[]> currentList = new ArrayList<>();
        List<int[]> nextList = new ArrayList<>();
        nextList.add(new int[]{x, z});

        int worldHeight = world.getHeight();
        while (nextList.size() > 0 && currentY < worldHeight)
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

                boolean isFoliage = curBlock.isFoliage(world, currentX, currentY, currentZ) || curBlock.getMaterial() == Material.leaves || curBlock.getMaterial() == Material.plants || curBlock.getMaterial() == Material.wood;
                boolean isCommon = curBlock == Blocks.stone || curBlock == Blocks.dirt || curBlock == Blocks.sand || curBlock == Blocks.stained_hardened_clay || curBlock == Blocks.gravel;
                boolean replaceable = currentY == y || curBlock == topBlock || curBlock == fillerBlock || curBlock.isReplaceable(world, currentX, currentY, currentZ)
                        || isCommon || isFoliage;
                if (replaceable)
                {
                    world.setBlockToAir(currentX, currentY, currentZ);
                }

                if (replaceable || curBlock.getMaterial() == Material.air)
                {
                    double distToOrigSQ = IvMathHelper.distanceSQ(new double[]{x, y, z}, new double[]{currentX, currentY, currentZ});
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

            currentY++;
        }
    }

    private void addIfNew(List<int[]> list, int... object)
    {
        if (!list.contains(object))
        {
            list.add(object);
        }
    }

    @Override
    public String displayString()
    {
        return "Natural Air: " + sourceBlock.getLocalizedName();
    }

    @Override
    public boolean generatesBefore()
    {
        return true;
    }
}
