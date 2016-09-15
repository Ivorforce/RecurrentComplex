/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.worldgen.sapling;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.structures.*;
import ivorius.reccomplex.structures.generic.gentypes.SaplingGenerationInfo;
import ivorius.reccomplex.utils.BlockSurfacePos;
import ivorius.reccomplex.utils.RCFunctions;
import ivorius.reccomplex.worldgen.StructureGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 14.09.16.
 */
public class RCSaplingGenerator
{
    public static boolean maybeGrowSapling(WorldServer world, BlockPos pos, Random random)
    {
        Environment environment = Environment.inNature(world, new StructureBoundingBox(pos, pos));

        List<Pair<StructureInfo, SaplingGenerationInfo>> applicable = Lists.newArrayList(StructureRegistry.INSTANCE.getStructureGenerations(
                SaplingGenerationInfo.class, pair -> pair.getRight().generatesIn(environment)
        ));

        ImmutableMultimap<Integer, Pair<StructureInfo, SaplingGenerationInfo>> groups = RCFunctions.groupMap(applicable, pair -> pair.getRight().pattern.pattern.compile(true).size());
        List<Integer> complexities = Lists.newArrayList(groups.keys());
        Collections.sort(complexities);

        Pair<StructureInfo, SaplingGenerationInfo> pair = null;
        while (complexities.size() > 0 && pair == null)
        {
            Integer complexity = complexities.remove(complexities.size() - 1);
            Set<Pair<StructureInfo, SaplingGenerationInfo>> placeable = groups.get(complexity).stream()
                    .filter(p -> p.getRight().pattern.canPlace(world, pos, p.getLeft().structureBoundingBox(), p.getLeft().isRotatable(), p.getLeft().isMirrorable()))
                    .collect(Collectors.toSet());

            double totalWeight = placeable.stream().mapToDouble(p -> p.getRight().getActiveWeight()).sum();

            if (complexity == 1)
            {
                // Vanilla as a simulated entry

                if (random.nextDouble() * (totalWeight * RCConfig.baseSaplingSpawnWeight + 1) < 1)
                    return false;
            }

            if (totalWeight > 0)
                pair = WeightedSelector.select(random, placeable, p -> p.getRight().getActiveWeight());
        }

        if (pair == null) // Generate default
            return false;

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
                .random(random).maturity(StructureGenerator.Maturity.SUGGEST).memorize(false)
                .randomPosition(BlockSurfacePos.from(spawnPos), (w, r, b) -> spawnPos.getY()).generate();

        return true;
    }
}
