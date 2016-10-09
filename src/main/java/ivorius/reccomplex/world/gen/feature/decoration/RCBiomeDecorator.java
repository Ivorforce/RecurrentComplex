/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.decoration;

import com.google.gson.annotations.SerializedName;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.utils.BlockSurfacePos;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import ivorius.reccomplex.world.gen.feature.selector.StructureSelector;
import ivorius.reccomplex.world.gen.feature.structure.Placer;
import ivorius.reccomplex.world.gen.feature.structure.StructureInfo;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.VanillaDecorationGenerationInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 16.09.16.
 */
public class RCBiomeDecorator
{
    public static Adapter vanillaAdapter = null;
    public static final List<Adapter> adapters = new ArrayList<>();

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
        Adapter adapter = adapter(worldIn, chunkPos, type, biomeIn, decorator);

        StructureSelector<VanillaDecorationGenerationInfo, DecorationType> selector = StructureRegistry.INSTANCE.decorationSelectors()
                .get(biomeIn, worldIn.provider);

        double totalWeight = selector.totalWeight(type);

        if (totalWeight <= 0)
            return false;

        int vanillaAmount = adapter.amount(worldIn, random, biomeIn, decorator, chunkPos, type);
        if (vanillaAmount < 0) return false; // don't interfere
        vanillaAmount = trySurface(worldIn, random, chunkPos, selector, type, totalWeight, vanillaAmount,
                adapter.forceUse(worldIn, random, biomeIn, decorator, chunkPos, type));
        for (int i = 0; i < vanillaAmount; ++i)
            adapter.generate(worldIn, random, biomeIn, decorator, chunkPos, type);

        return vanillaAmount >= 0; // if -1 we let vanilla do its thing
    }

    public static Adapter adapter(WorldServer worldIn, BlockPos chunkPos, DecorationType type, Biome biomeIn, BiomeDecorator decorator)
    {
        return adapters.stream().filter(a -> a.matches(worldIn, biomeIn, decorator, chunkPos, type)).findFirst().orElse(vanillaAdapter);
    }

    public static int trySurface(WorldServer worldIn, Random random, BlockPos chunkPos, StructureSelector<VanillaDecorationGenerationInfo, DecorationType> selector, DecorationType type, double totalWeight, int vanillaAmount, boolean lowChance)
    {
        int rcAmount = amount(random, totalWeight, vanillaAmount);

        // When the chance is low, we don't give back to vanilla to try once again, to avoid double the spawn rate
        if (rcAmount <= 0 && !lowChance) return -1;

        for (int i = 0; i < rcAmount; i++)
            generateSurface(selector.selectOne(random, type, totalWeight), worldIn, chunkPos, random);

        return vanillaAmount - rcAmount;
    }

    public static void generateSurface(Pair<StructureInfo, VanillaDecorationGenerationInfo> generation, WorldServer worldIn, BlockPos chunkPos, Random random)
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

    interface Adapter
    {
        /**
         * If this adapter matches the environment and should be used.
         */
        boolean matches(WorldServer worldIn, Biome biome, BiomeDecorator decorator, BlockPos chunkPos, DecorationType type);

        /**
         * The amount of things generated
         * @return The amount. < 0 if ReC should not override decoration here.
         */
        int amount(WorldServer worldIn, Random random, Biome biome, BiomeDecorator decorator, BlockPos chunkPos, DecorationType type);

        /**
         * If even if no ReC structures are generated, we shouldn't give back to vanilla, to avoid doubling spawn rates.
         */
        boolean forceUse(WorldServer worldIn, Random random, Biome biome, BiomeDecorator decorator, BlockPos chunkPos, DecorationType type);

        /**
         * Generate one thing in the biome
         */
        void generate(WorldServer worldIn, Random random, Biome biome, BiomeDecorator decorator, BlockPos chunkPos, DecorationType type);
    }
}
