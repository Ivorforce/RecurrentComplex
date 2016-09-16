/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.worldgen.decoration;

import com.google.gson.annotations.SerializedName;
import ivorius.ivtoolkit.random.WeightedSelector;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.structures.Environment;
import ivorius.reccomplex.structures.StructureInfo;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.structures.generic.gentypes.VanillaDecorationGenerationInfo;
import ivorius.reccomplex.utils.BlockSurfacePos;
import ivorius.reccomplex.worldgen.StructureGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Random;

/**
 * Created by lukas on 16.09.16.
 */
public class RCBiomeDecorator
{
    @ParametersAreNonnullByDefault
    public static boolean decorate(WorldServer worldIn, Random random, BlockPos chunkPos, DecorationType type)
    {
        try
        {
            return doDecorate(worldIn, random, chunkPos, type);
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.error("Exception when decorating", e);
        }

        return false;
    }

    protected static boolean doDecorate(WorldServer worldIn, Random random, BlockPos chunkPos, DecorationType type)
    {
        double baseWeight = RCConfig.baseDecorationWeights.get(type);

        if (baseWeight <= 0)
            return false;

        Biome biomeIn = worldIn.getBiome(chunkPos.add(16, 0, 16));
        Environment baseEnv = new Environment(worldIn, biomeIn, null, null);
        BiomeDecorator decorator = biomeIn.theBiomeDecorator;

        Collection<Pair<StructureInfo, VanillaDecorationGenerationInfo>> generations = StructureRegistry.INSTANCE.getStructureGenerations(VanillaDecorationGenerationInfo.class,
                pair -> pair.getRight().generatesIn(baseEnv.withGeneration(pair.getRight()))
        );

        double totalWeight = generations.stream().mapToDouble(pair -> pair.getRight().getActiveWeight()).sum() * baseWeight;

        if (totalWeight <= 0)
            return false;

        switch (type)
        {
            case TREE:
            {
                int vanillaAmount = decorator.treesPerChunk;

                if (random.nextFloat() < decorator.field_189870_A)
                    ++vanillaAmount;

                if ((vanillaAmount = trySurface(worldIn, random, chunkPos, generations, totalWeight, vanillaAmount)) < 0)
                    return false;

                for (int j2 = 0; j2 < vanillaAmount; ++j2)
                {
                    int k6 = random.nextInt(16) + 8;
                    int l = random.nextInt(16) + 8;
                    WorldGenAbstractTree worldgenabstracttree = biomeIn.genBigTreeChance(random);
                    worldgenabstracttree.setDecorationDefaults();
                    BlockPos blockpos = worldIn.getHeight(chunkPos.add(k6, 0, l));

                    if (worldgenabstracttree.generate(worldIn, random, blockpos))
                    {
                        worldgenabstracttree.generateSaplings(worldIn, random, blockpos);
                    }
                }

                return true;
            }
            case BIG_SHROOM:
            {
                int vanillaAmount = decorator.bigMushroomsPerChunk;

                if ((vanillaAmount = trySurface(worldIn, random, chunkPos, generations, totalWeight, vanillaAmount)) < 0)
                    return false;

                for (int k2 = 0; k2 < vanillaAmount; ++k2)
                {
                    int l6 = random.nextInt(16) + 8;
                    int k10 = random.nextInt(16) + 8;
                    decorator.bigMushroomGen.generate(worldIn, random, worldIn.getHeight(chunkPos.add(l6, 0, k10)));
                }

                return false;
            }
            case CACTUS:
            {
                int vanillaAmount = decorator.cactiPerChunk;

                if ((vanillaAmount = trySurface(worldIn, random, chunkPos, generations, totalWeight, vanillaAmount)) < 0)
                    return false;

                for (int j5 = 0; j5 < vanillaAmount; ++j5)
                {
                    int l9 = random.nextInt(16) + 8;
                    int k13 = random.nextInt(16) + 8;
                    int l16 = worldIn.getHeight(chunkPos.add(l9, 0, k13)).getY() * 2;

                    if (l16 > 0)
                    {
                        int j19 = random.nextInt(l16);
                        decorator.cactusGen.generate(worldIn, random, chunkPos.add(l9, j19, k13));
                    }
                }
                return false;
            }
            default:
                return false;
        }
    }

    protected static int trySurface(WorldServer worldIn, Random random, BlockPos chunkPos, Collection<Pair<StructureInfo, VanillaDecorationGenerationInfo>> generations, double totalWeight, int vanillaAmount)
    {
        int rcAmount = amount(random, totalWeight, vanillaAmount);

        if (rcAmount <= 0) return -1;
        generateSurface(worldIn, random, chunkPos, generations, rcAmount);

        return vanillaAmount - rcAmount;
    }

    protected static void generateSurface(WorldServer worldIn, Random random, BlockPos chunkPos, Collection<Pair<StructureInfo, VanillaDecorationGenerationInfo>> generations, int rcAmount)
    {
        for (int i = 0; i < rcAmount; i++)
            generateSurface(WeightedSelector.select(random, generations, pair -> pair.getRight().getActiveWeight()), worldIn, chunkPos, random);
    }

    protected static void generateSurface(Pair<StructureInfo, VanillaDecorationGenerationInfo> generation, WorldServer worldIn, BlockPos chunkPos, Random random)
    {
        new StructureGenerator<>(generation.getLeft()).generationInfo(generation.getRight()).world(worldIn)
                .random(random)
                .maturity(StructureSpawnContext.GenerateMaturity.SUGGEST).memorize(false)
                .randomPosition(randomSurfacePos(random, chunkPos), generation.getRight().ySelector()).fromCenter(true)
                .generate();
    }

    protected static BlockSurfacePos randomSurfacePos(Random random, BlockPos chunkPos)
    {
        return BlockSurfacePos.from(chunkPos).add(random.nextInt(16) + 8, random.nextInt(16) + 8);
    }

    protected static int amount(Random random, double totalWeight, int picks)
    {
        int amount = 0;
        for (int i = 0; i < picks; i++)
        {
            if (random.nextDouble() * (totalWeight + 1) > 1)
                amount++;
        }
        return amount;
    }

    public enum DecorationType
    {
        @SerializedName("big_mushroom")
        BIG_SHROOM,
        @SerializedName("cactus")
        CACTUS,
        @SerializedName("tree")
        TREE;

        public static DecorationType byID(String id)
        {
            return IvGsonHelper.enumForName(id, values());
        }

        public String id()
        {
            return IvGsonHelper.serializedName(this);
        }
    }
}
