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
import ivorius.reccomplex.world.gen.feature.structure.StructureInfo;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.SaplingGenerationInfo;
import ivorius.reccomplex.utils.BlockSurfacePos;
import ivorius.reccomplex.utils.RCFunctions;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

        // Hackily consider big vanilla trees too
        VanillaType vanillaType = vanillaType(world, pos, world.getBlockState(pos), random);
        int vanillaComplexity = vanillaType == VanillaType.NORMAL ? 1 : vanillaType == VanillaType.BIG ? 4 : -1;

        ImmutableMultimap<Integer, Pair<StructureInfo, SaplingGenerationInfo>> groups = RCFunctions.groupMap(applicable, pair -> pair.getRight().pattern.pattern.compile(true).size());
        List<Integer> complexities = Lists.newArrayList(groups.keys());
        if (vanillaComplexity > 0) complexities.add(vanillaComplexity);
        Collections.sort(complexities);

        Pair<StructureInfo, SaplingGenerationInfo> pair = null;
        while (complexities.size() > 0 && pair == null)
        {
            Integer complexity = complexities.remove(complexities.size() - 1);
            Set<Pair<StructureInfo, SaplingGenerationInfo>> placeable = groups.get(complexity).stream()
                    .filter(p -> p.getRight().pattern.canPlace(world, pos, p.getLeft().structureBoundingBox(), p.getLeft().isRotatable(), p.getLeft().isMirrorable()))
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

    protected static double getSpawnWeight(Pair<StructureInfo, SaplingGenerationInfo> p)
    {
        return RCConfig.tweakedSpawnRate(StructureRegistry.INSTANCE.id(p.getLeft())) * p.getRight().getActiveWeight();
    }

    public static void growSapling(WorldServer world, BlockPos pos, Random random, StructureInfo structure, SaplingGenerationInfo saplingGenInfo)
    {
        int[] strucSize = structure.structureBoundingBox();

        Multimap<AxisAlignedTransform2D, BlockPos> placeables = saplingGenInfo.pattern.testAll(world, pos, strucSize, structure.isRotatable(), structure.isMirrorable());

        AxisAlignedTransform2D transform = Lists.newArrayList(placeables.keySet()).get(random.nextInt(placeables.keySet().size()));
        Collection<BlockPos> transformedPositions = placeables.get(transform);
        BlockPos startPos = Lists.newArrayList(transformedPositions).get(random.nextInt(transformedPositions.size()));

        Map<BlockPos, IBlockState> before = new HashMap<>();
        IBlockState air = Blocks.AIR.getDefaultState();
        saplingGenInfo.pattern.copy(transform, strucSize).forEach(i -> i.delete, entry ->
        {
            BlockPos ePos = entry.getKey().add(startPos);
            before.put(ePos, world.getBlockState(ePos));
            world.setBlockState(ePos, air, 4);
        });

        BlockPos spawnPos = transform.apply(saplingGenInfo.spawnShift, new int[]{1, 1, 1}).add(startPos);

        boolean success = new StructureGenerator<>(structure).world(world).generationInfo(saplingGenInfo)
                .transform(transform).random(random).maturity(StructureSpawnContext.GenerateMaturity.SUGGEST)
                .memorize(RCConfig.memorizeSaplings).allowOverlaps(true)
                .randomPosition(BlockSurfacePos.from(spawnPos), (context, blockCollection) -> spawnPos.getY()).generate().isPresent();

        if (!success)
            before.forEach((pos1, state) -> world.setBlockState(pos1, state, 4));
    }

    // From BlockSapling
    public static VanillaType vanillaType(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (!(state.getBlock() == Blocks.SAPLING))
            return VanillaType.NORMAL;

        int i = 0;
        int j = 0;
        boolean flag = false;

        switch (state.getValue(BlockSapling.TYPE))
        {
            case SPRUCE:
                label114:

                for (i = 0; i >= -1; --i)
                {
                    for (j = 0; j >= -1; --j)
                    {
                        if (isTwoByTwoOfType(worldIn, pos, i, j, BlockPlanks.EnumType.SPRUCE))
                        {
                            flag = true;
                            break label114;
                        }
                    }
                }

                if (!flag)
                {
                    i = 0;
                    j = 0;
                }

                break;
            case BIRCH:
                break;
            case JUNGLE:
                label269:

                for (i = 0; i >= -1; --i)
                {
                    for (j = 0; j >= -1; --j)
                    {
                        if (isTwoByTwoOfType(worldIn, pos, i, j, BlockPlanks.EnumType.JUNGLE))
                        {
                            flag = true;
                            break label269;
                        }
                    }
                }

                if (!flag)
                {
                    i = 0;
                    j = 0;
                }

                break;
            case ACACIA:
                break;
            case DARK_OAK:
                label390:

                for (i = 0; i >= -1; --i)
                {
                    for (j = 0; j >= -1; --j)
                    {
                        if (isTwoByTwoOfType(worldIn, pos, i, j, BlockPlanks.EnumType.DARK_OAK))
                        {
                            flag = true;
                            break label390;
                        }
                    }
                }

                if (!flag)
                {
                    return VanillaType.NONE;
                }

            case OAK:
        }

        return flag ? VanillaType.BIG : VanillaType.NORMAL;
    }

    private static boolean isTwoByTwoOfType(World worldIn, BlockPos pos, int p_181624_3_, int p_181624_4_, BlockPlanks.EnumType type)
    {
        BlockSapling sapling = (BlockSapling) Blocks.SAPLING;
        return sapling.isTypeAt(worldIn, pos.add(p_181624_3_, 0, p_181624_4_), type) && sapling.isTypeAt(worldIn, pos.add(p_181624_3_ + 1, 0, p_181624_4_), type) && sapling.isTypeAt(worldIn, pos.add(p_181624_3_, 0, p_181624_4_ + 1), type) && sapling.isTypeAt(worldIn, pos.add(p_181624_3_ + 1, 0, p_181624_4_ + 1), type);
    }

    enum VanillaType
    {
        NORMAL, BIG, NONE
    }
}
