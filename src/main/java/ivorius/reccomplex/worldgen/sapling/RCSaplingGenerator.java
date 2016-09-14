/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.worldgen.sapling;

import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.structures.*;
import ivorius.reccomplex.structures.generic.gentypes.SaplingGenerationInfo;
import ivorius.reccomplex.utils.BlockSurfacePos;
import ivorius.reccomplex.worldgen.StructureGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Random;

/**
 * Created by lukas on 14.09.16.
 */
public class RCSaplingGenerator
{
    public static boolean maybeGrowSapling(WorldServer world, BlockPos pos, Random random)
    {
        Environment environment = Environment.inNature(world, new StructureBoundingBox(pos, pos));
        IBlockState blockState = world.getBlockState(pos);

        Collection<Pair<StructureInfo, SaplingGenerationInfo>> generations = StructureRegistry.INSTANCE.getStructureGenerations(SaplingGenerationInfo.class, pair -> pair.getRight().generatesFor(environment, blockState));
        double totalWeight = generations.stream().mapToDouble(p -> p.getRight().getActiveWeight()).sum();

        if (random.nextDouble() * (totalWeight * RCConfig.baseSaplingSpawnWeight + 1) < 1)
            return false; // Generate default

        world.setBlockToAir(pos);

        Pair<StructureInfo, SaplingGenerationInfo> generation = WeightedSelector.select(random, generations, p -> p.getRight().getActiveWeight());
        BlockPos spawnPos = pos.add(generation.getRight().spawnShift);

        YSelector ySelector = (world1, random1, boundingBox) -> spawnPos.getY();

        new StructureGenerator<>(generation.getLeft()).world(world)
                .random(random).maturity(StructureGenerator.Maturity.SUGGEST)
                .randomPosition(BlockSurfacePos.from(spawnPos), ySelector).fromCenter(true).generate();

        return true;
    }
}
