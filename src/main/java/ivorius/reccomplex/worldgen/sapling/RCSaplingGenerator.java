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
import ivorius.reccomplex.structures.Environment;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.gentypes.SaplingGenerationInfo;
import ivorius.reccomplex.utils.BlockSurfacePos;
import ivorius.reccomplex.utils.RCFunctions;
import ivorius.reccomplex.worldgen.StructureGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by lukas on 14.09.16.
 */
public class RCSaplingGenerator
{
    public static boolean maybeGrowSapling(WorldServer world, BlockPos pos, Random random)
    {
        if (RCConfig.saplingTriggerChance <= 0 || (RCConfig.saplingTriggerChance < 1 && random.nextFloat() < RCConfig.saplingTriggerChance))
            return false; // Don't trigger at all

        Pair<StructureInfo, SaplingGenerationInfo> pair = findRandomSapling(world, pos, random, true);

        if (pair == null) // Generate default
            return false;

        growSapling(world, pos, random, pair.getLeft(), pair.getRight());

        return true;
    }

    @Nullable
    public static Pair<StructureInfo, SaplingGenerationInfo> findRandomSapling(WorldServer world, BlockPos pos, Random random, boolean considerVanilla)
    {
        Environment baseEnv = Environment.inNature(world, new StructureBoundingBox(pos, pos));

        List<Pair<StructureInfo, SaplingGenerationInfo>> applicable = Lists.newArrayList(StructureRegistry.INSTANCE.getStructureGenerations(
                SaplingGenerationInfo.class, pair -> pair.getRight().generatesIn(baseEnv.withGeneration(pair.getRight()))
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

            if (complexity == 1 && considerVanilla)
            {
                // Vanilla as a simulated entry

                if (random.nextDouble() * (totalWeight * RCConfig.baseSaplingSpawnWeight + 1) < 1)
                    break;
            }

            if (totalWeight > 0)
                pair = WeightedSelector.select(random, placeable, p -> p.getRight().getActiveWeight());
        }

        return pair;
    }

    public static void growSapling(WorldServer world, BlockPos pos, Random random, StructureInfo structure, SaplingGenerationInfo saplingGenInfo)
    {
        int[] strucSize = structure.structureBoundingBox();

        Multimap<AxisAlignedTransform2D, BlockPos> placeables = saplingGenInfo.pattern.testAll(world, pos, strucSize, structure.isRotatable(), structure.isMirrorable());

        AxisAlignedTransform2D transform = Lists.newArrayList(placeables.keySet()).get(random.nextInt(placeables.keySet().size()));
        Collection<BlockPos> transformedPositions = placeables.get(transform);
        BlockPos startPos = Lists.newArrayList(transformedPositions).get(random.nextInt(transformedPositions.size()));

        Map<BlockPos, IBlockState> before = new HashMap<>();
        Consumer<Map.Entry<BlockPos, String>> consumer = entry ->
        {
            BlockPos ePos = entry.getKey().add(startPos);
            before.put(ePos, world.getBlockState(ePos));
            world.setBlockToAir(ePos);
        };
        saplingGenInfo.pattern.copy(transform, strucSize).forEach(consumer);

        BlockPos spawnPos = transform.apply(saplingGenInfo.spawnShift, new int[]{1, 1, 1}).add(startPos);

        boolean success = new StructureGenerator<>(structure).world(world).generationInfo(saplingGenInfo)
                .transform(transform).random(random).maturity(StructureSpawnContext.GenerateMaturity.SUGGEST)
                .memorize(RCConfig.memorizeSaplings).allowOverlaps(true)
                .randomPosition(BlockSurfacePos.from(spawnPos), (context, blockCollection) -> spawnPos.getY()).generate() != null;

        if (!success)
            before.forEach(world::setBlockState);
    }
}
