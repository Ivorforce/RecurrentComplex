/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.sapling;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.world.gen.feature.structure.Environment;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.SaplingGeneration;
import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.ivtoolkit.util.IvFunctions;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lukas on 14.09.16.
 */
public class RCSaplingGenerator
{
    public static final List<Predictor> predictors = new ArrayList<>();

    static {
        predictors.add(new VanillaSaplingPredictor());
    }

    public static boolean maybeGrowSapling(WorldServer world, BlockPos pos, Random random)
    {
        if (RCConfig.saplingTriggerChance <= 0 || (RCConfig.saplingTriggerChance < 1 && random.nextFloat() < RCConfig.saplingTriggerChance))
            return false; // Don't trigger at all

        Pair<Structure<?>, SaplingGeneration> pair = findRandomSapling(world, pos, random, true);

        if (pair == null) // Generate default
            return false;

        growSapling(world, pos, random, pair.getLeft(), pair.getRight());

        return true;
    }

    @Nullable
    public static Pair<Structure<?>, SaplingGeneration> findRandomSapling(WorldServer world, BlockPos pos, Random random, boolean considerVanilla)
    {
        Environment baseEnv = Environment.inNature(world, new StructureBoundingBox(pos, pos));

        List<Pair<Structure<?>, SaplingGeneration>> applicable = StructureRegistry.INSTANCE.getGenerationTypes(SaplingGeneration.class).stream()
                .filter(pair1 -> pair1.getRight().generatesIn(baseEnv.withGeneration(pair1.getRight())))
                .collect(Collectors.toCollection(ArrayList::new));

        // Hackily consider big vanilla trees too
        int vanillaComplexity = complexity(world, pos, random, predictors);

        ImmutableMultimap<Integer, Pair<Structure<?>, SaplingGeneration>> groups = IvFunctions.groupMap(applicable, pair -> pair.getRight().pattern.pattern.compile(true).size());
        List<Integer> complexities = Lists.newArrayList(groups.keySet());
        if (vanillaComplexity > 0) complexities.add(vanillaComplexity);
        Collections.sort(complexities);

        Pair<Structure<?>, SaplingGeneration> pair = null;
        while (complexities.size() > 0 && pair == null)
        {
            Integer complexity = complexities.remove(complexities.size() - 1);
            Set<Pair<Structure<?>, SaplingGeneration>> placeable = groups.get(complexity).stream()
                    .filter(p -> p.getRight().pattern.canPlace(world, pos, p.getLeft().size(), p.getLeft().isRotatable(), p.getLeft().isMirrorable()))
                    .collect(Collectors.toSet());

            double totalWeight = placeable.stream().mapToDouble(RCSaplingGenerator::getSpawnWeight).sum();

            if (complexity == vanillaComplexity && considerVanilla)
            {
                // Vanilla as a simulated entry

                if (random.nextDouble() * (totalWeight * RCConfig.baseSaplingSpawnWeight + 1) < 1)
                    break;
            }

            if (totalWeight > 0)
                pair = WeightedSelector.select(random, placeable, RCSaplingGenerator::getSpawnWeight);
        }

        return pair;
    }

    public static int complexity(WorldServer world, BlockPos pos, Random random, List<Predictor> predictors)
    {
        return predictors.stream()
                .mapToInt(a -> a.complexity(world, pos, world.getBlockState(pos), random))
                .filter(i -> i >= 0).max().orElse(-1);
    }

    public static double getSpawnWeight(Pair<Structure<?>, SaplingGeneration> p)
    {
        return RCConfig.tweakedSpawnRate(StructureRegistry.INSTANCE.id(p.getLeft())) * p.getRight().getActiveWeight();
    }

    public static void growSapling(WorldServer world, BlockPos pos, Random random, Structure<?> structure, SaplingGeneration saplingGenInfo)
    {
        int[] strucSize = structure.size();

        Multimap<AxisAlignedTransform2D, BlockPos> placeables = saplingGenInfo.pattern.testAll(world, pos, strucSize, structure.isRotatable(), structure.isMirrorable());

        // Use keys() here to get the correct distribution
        AxisAlignedTransform2D transform = Lists.newArrayList(placeables.keys()).get(random.nextInt(placeables.keys().size()));
        Collection<BlockPos> transformedPositions = placeables.get(transform);
        BlockPos startPos = Lists.newArrayList(transformedPositions).get(random.nextInt(transformedPositions.size()));

        Map<BlockPos, IBlockState> before = new HashMap<>();
        IBlockState air = Blocks.AIR.getDefaultState();
        saplingGenInfo.pattern.copy(transform, strucSize).forEach(i -> i.delete, (p, i) ->
        {
            BlockPos ePos = p.add(startPos);
            before.put(ePos, world.getBlockState(ePos));
            world.setBlockState(ePos, air, 4);
        });

        BlockPos spawnPos = transform.apply(saplingGenInfo.spawnShift, new int[]{1, 1, 1}).add(startPos);

        boolean success = new StructureGenerator<>(structure).world(world).generationInfo(saplingGenInfo)
                .transform(transform).seed(random.nextLong()).maturity(StructureSpawnContext.GenerateMaturity.SUGGEST)
                .memorize(RCConfig.memorizeSaplings).allowOverlaps(true)
                .randomPosition(BlockSurfacePos.from(spawnPos), (context, blockCollection) -> spawnPos.getY()).generate().succeeded();

        if (!success)
            before.forEach((pos1, state) -> world.setBlockState(pos1, state, 4));
    }

    interface Predictor
    {
        /**
         * The complexity of this tree type. The highest complexity tree will always generate.
         * @return The complexity. -1 for 'I wouldn't even consider generating'.
         */
        int complexity(World worldIn, BlockPos pos, IBlockState state, Random rand);
    }
}
