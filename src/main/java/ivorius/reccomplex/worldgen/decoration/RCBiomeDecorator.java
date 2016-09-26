/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.worldgen.decoration;

import com.google.gson.annotations.SerializedName;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.structures.*;
import ivorius.reccomplex.structures.generic.gentypes.VanillaDecorationGenerationInfo;
import ivorius.reccomplex.utils.BlockSurfacePos;
import ivorius.reccomplex.worldgen.StructureGenerator;
import ivorius.reccomplex.worldgen.selector.StructureSelector;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenDesertWells;
import net.minecraft.world.gen.feature.WorldGenFossils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.ParametersAreNonnullByDefault;
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
        BiomeDecorator decorator = biomeIn.theBiomeDecorator;

        StructureSelector<VanillaDecorationGenerationInfo, DecorationType> selector = StructureRegistry.INSTANCE.decorationSelectors()
                .get(biomeIn, worldIn.provider);

        double totalWeight = selector.totalWeight(type);

        if (totalWeight <= 0)
            return false;

        if ((biomeIn == Biomes.ROOFED_FOREST || biomeIn == Biomes.MUTATED_ROOFED_FOREST)
                && (type == DecorationType.TREE || type == DecorationType.BIG_SHROOM))
            return false; // This is the roofed forest override, don't touch because the event impl is shit

        switch (type)
        {
            case TREE:
            {
                int vanillaAmount = decorator.treesPerChunk;

                if (random.nextFloat() < decorator.field_189870_A)
                    ++vanillaAmount;

                if ((vanillaAmount = trySurface(worldIn, random, chunkPos, selector, type, totalWeight, vanillaAmount, decorator.treesPerChunk <= 0)) < 0)
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

                if ((vanillaAmount = trySurface(worldIn, random, chunkPos, selector, type, totalWeight, vanillaAmount, false)) < 0)
                    return false;

                for (int k2 = 0; k2 < vanillaAmount; ++k2)
                {
                    int l6 = random.nextInt(16) + 8;
                    int k10 = random.nextInt(16) + 8;
                    decorator.bigMushroomGen.generate(worldIn, random, worldIn.getHeight(chunkPos.add(l6, 0, k10)));
                }

                return true;
            }
            case CACTUS:
            {
                int vanillaAmount = decorator.cactiPerChunk;

                if ((vanillaAmount = trySurface(worldIn, random, chunkPos, selector, type, totalWeight, vanillaAmount, false)) < 0)
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

                return true;
            }
            case DESERT_WELL:
            {
                int vanillaAmount = random.nextInt(100) == 0 ? 1 : 0;

                if ((vanillaAmount = trySurface(worldIn, random, chunkPos, selector, type, totalWeight, vanillaAmount, true)) < 0)
                    return false;

                for (int j5 = 0; j5 < vanillaAmount; ++j5)
                {
                    int i = random.nextInt(16) + 8;
                    int j = random.nextInt(16) + 8;
                    BlockPos blockpos = worldIn.getHeight(chunkPos.add(i, 0, j)).up();
                    (new WorldGenDesertWells()).generate(worldIn, random, blockpos);
                }

                return true;
            }
            case FOSSIL:
            {
                int vanillaAmount = random.nextInt(64) == 0 ? 1 : 0;

                if ((vanillaAmount = trySurface(worldIn, random, chunkPos, selector, type, totalWeight, vanillaAmount, true)) < 0)
                    return false;

                for (int j5 = 0; j5 < vanillaAmount; ++j5)
                {
                    (new WorldGenFossils()).generate(worldIn, random, chunkPos);
                }
            }
            default:
                return false;
        }
    }

    protected static int trySurface(WorldServer worldIn, Random random, BlockPos chunkPos, StructureSelector<VanillaDecorationGenerationInfo, DecorationType> selector, DecorationType type, double totalWeight, int vanillaAmount, boolean lowChance)
    {
        int rcAmount = amount(random, totalWeight, vanillaAmount);

        // When the chance is low, we don't give back to vanilla to try once again, to avoid double the spawn rate
        if (rcAmount <= 0 && !lowChance) return -1;

        for (int i = 0; i < rcAmount; i++)
            generateSurface(selector.selectOne(random, type, totalWeight), worldIn, chunkPos, random);

        return vanillaAmount - rcAmount;
    }

    protected static void generateSurface(Pair<StructureInfo, VanillaDecorationGenerationInfo> generation, WorldServer worldIn, BlockPos chunkPos, Random random)
    {
        new StructureGenerator<>(generation.getLeft()).generationInfo(generation.getRight()).world(worldIn)
                .random(random).maturity(StructureSpawnContext.GenerateMaturity.SUGGEST)
                .memorize(RCConfig.memorizeDecoration).allowOverlaps(true)
                .randomPosition(randomSurfacePos(random, chunkPos.add(generation.getRight().spawnShift)), // Shift +1 because surface placer goes -1
                        shift(generation.getRight().placer(), generation.getRight().spawnShift.getY() + 1)).fromCenter(true)
                .generate();
    }

    protected static Placer shift(Placer placer, int y)
    {
        return (context, blockCollection) -> placer.place(context, blockCollection) + y;
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
        TREE,
        @SerializedName("desert_well")
        DESERT_WELL,
        @SerializedName("fossil")
        FOSSIL;

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
