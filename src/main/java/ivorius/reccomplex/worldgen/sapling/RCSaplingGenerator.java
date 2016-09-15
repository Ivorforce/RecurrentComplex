/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.worldgen.sapling;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.structures.*;
import ivorius.reccomplex.structures.generic.gentypes.SaplingGenerationInfo;
import ivorius.reccomplex.utils.BlockSurfacePos;
import ivorius.reccomplex.worldgen.StructureGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by lukas on 14.09.16.
 */
public class RCSaplingGenerator
{
    public static boolean maybeGrowSapling(WorldServer world, BlockPos pos, Random random)
    {
        Environment environment = Environment.inNature(world, new StructureBoundingBox(pos, pos));

        Collection<Pair<StructureInfo, SaplingGenerationInfo>> generations = StructureRegistry.INSTANCE.getStructureGenerations(
                SaplingGenerationInfo.class, pair -> pair.getRight().generatesIn(environment)
                && pair.getRight().pattern.canPlace(world, pos, pair.getLeft().structureBoundingBox(), pair.getLeft().isRotatable(), pair.getLeft().isMirrorable())
        );
        double totalWeight = generations.stream().mapToDouble(p -> p.getRight().getActiveWeight()).sum();

        if (random.nextDouble() * (totalWeight * RCConfig.baseSaplingSpawnWeight + 1) < 1)
            return false; // Generate default

        Pair<StructureInfo, SaplingGenerationInfo> pair = WeightedSelector.select(random, generations, p -> p.getRight().getActiveWeight());
        StructureInfo structure = pair.getLeft();
        SaplingGenerationInfo saplingGenInfo = pair.getRight();
        int[] strucSize = structure.structureBoundingBox();

        Multimap<AxisAlignedTransform2D, BlockPos> placeables = saplingGenInfo.pattern.testAll(world, pos, strucSize, structure.isRotatable(), structure.isMirrorable());

        AxisAlignedTransform2D transform = Lists.newArrayList(placeables.keySet()).get(random.nextInt(placeables.keySet().size()));
        Collection<BlockPos> transformedPositions = placeables.get(transform);
        BlockPos startPos = Lists.newArrayList(transformedPositions).get(random.nextInt(transformedPositions.size()));

        saplingGenInfo.pattern.setToAir(world, startPos, transform, strucSize);

        BlockPos spawnPos = transform.apply(saplingGenInfo.spawnShift, new int[]{1, 1, 1}).add(startPos);

        new StructureGenerator<>(structure).world(world).transform(transform)
                .random(random).maturity(StructureGenerator.Maturity.FIRST)
                .randomPosition(BlockSurfacePos.from(spawnPos), (w, r, b) -> spawnPos.getY()).generate();

        return true;
    }
}
