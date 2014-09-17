/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen.blockTransformers;

import ivorius.ivtoolkit.blocks.BlockArea;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.math.IvVecMathHelper;
import ivorius.ivtoolkit.tools.IvWorldData;
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
    public static final double NATURAL_DISTANCE_RANDOMIZATION = 10.0;

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
    public void apply(World world, Random random, Phase phase, BlockCoord coord, Block sourceBlock, int sourceMetadata, IvWorldData worldData)
    {
        BiomeGenBase biome = world.getBiomeGenForCoords(coord.x, coord.z);
        Block topBlock = biome.topBlock;
        Block fillerBlock = biome.fillerBlock;

        coord = coord.subtract(0, 4, 0);

        int currentY = coord.y;
        List<int[]> currentList = new ArrayList<>();
        List<int[]> nextList = new ArrayList<>();
        nextList.add(new int[]{coord.x, coord.z});

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
                boolean replaceable = currentY == coord.y || curBlock == topBlock || curBlock == fillerBlock || curBlock.isReplaceable(world, currentX, currentY, currentZ)
                        || isCommon || isFoliage;
                if (replaceable)
                {
                    world.setBlockToAir(currentX, currentY, currentZ);
                }

                if (replaceable || curBlock.getMaterial() == Material.air)
                {
                    double distToOrigSQ = IvVecMathHelper.distanceSQ(new double[]{coord.x, coord.y, coord.z}, new double[]{currentX, currentY, currentZ});
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

    @Override
    public void applyArea(World world, Random random, Phase phase, BlockArea area, AxisAlignedTransform2D transform2D, IvWorldData worldData)
    {

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
    public boolean generatesInPhase(Phase phase)
    {
        return phase == Phase.BEFORE;
    }

    @Override
    public boolean generatesAreaInPhase(Phase phase)
    {
        return false;
    }
}
