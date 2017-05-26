/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.decoration;

import com.google.gson.annotations.SerializedName;
import ivorius.ivtoolkit.tools.IvGsonHelper;
import ivorius.reccomplex.RCConfig;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.ivtoolkit.blocks.BlockSurfacePos;
import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import ivorius.reccomplex.world.gen.feature.selector.StructureSelector;
import ivorius.reccomplex.world.gen.feature.structure.Placer;
import ivorius.reccomplex.world.gen.feature.structure.Structure;
import ivorius.reccomplex.world.gen.feature.structure.StructureRegistry;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.VanillaDecorationGeneration;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraftforge.fml.common.eventhandler.Event;
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
    public static Adapter vanillaAdapter = new VanillaDecorationAdapter();
    public static final List<Adapter> adapters = new ArrayList<>();

    public static final int STRUCTURE_TRIES = 5;

    @ParametersAreNonnullByDefault
    public static Event.Result decorate(WorldServer worldIn, Random random, BlockPos chunkPos, DecorationType type)
    {
        try
        {
            return doDecorate(worldIn, random, chunkPos, type);
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.error("Exception when decorating", e);
        }

        return null;
    }

    protected static Event.Result doDecorate(WorldServer worldIn, Random random, BlockPos chunkPos, DecorationType type)
    {
        Biome biomeIn = worldIn.getBiome(chunkPos.add(16, 0, 16));
        BiomeDecorator decorator = biomeIn.decorator;
        Adapter adapter = adapter(worldIn, chunkPos, type, biomeIn, decorator);

        int origAmount = adapter.amount(worldIn, random, biomeIn, decorator, chunkPos, type);

        if (origAmount < 0) return null; // Don't interfere

        int vanillaAmount = doDecorate(worldIn, random, chunkPos, type, origAmount, adapter.mayGiveUp(worldIn, random, biomeIn, decorator, chunkPos, type));

        if (vanillaAmount == origAmount) return null; // Replaced none, might as well give back

        for (int i = 0; i < vanillaAmount; ++i)
            adapter.generate(worldIn, random, biomeIn, decorator, chunkPos, type);

        return vanillaAmount >= 0 ? Event.Result.DENY : null;  // if < 0 we let vanilla do its thing
    }

    @ParametersAreNonnullByDefault
    public static int decorate(WorldServer worldIn, Random random, BlockPos chunkPos, DecorationType type, int amount)
    {
        try
        {
            return doDecorate(worldIn, random, chunkPos, type, amount, false);
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.error("Exception when decorating", e);
        }

        return amount;
    }

    protected static int doDecorate(WorldServer worldIn, Random random, BlockPos chunkPos, DecorationType type, int amount, boolean lowChance)
    {
        double baseWeight = RCConfig.baseDecorationWeights.get(type);

        if (baseWeight <= 0)
            return amount;

        Biome biomeIn = worldIn.getBiome(chunkPos.add(16, 0, 16));

        StructureSelector<VanillaDecorationGeneration, DecorationType> selector = StructureRegistry.INSTANCE.decorationSelectors()
                .get(biomeIn, worldIn.provider);

        double totalWeight = selector.totalWeight(type);

        if (totalWeight <= 0)
            return amount;

        return trySurface(worldIn, random, chunkPos, selector, type, totalWeight, baseWeight, amount, lowChance);
    }

    public static Adapter adapter(WorldServer worldIn, BlockPos chunkPos, DecorationType type, Biome biomeIn, BiomeDecorator decorator)
    {
        return adapters.stream().filter(a -> a.matches(worldIn, biomeIn, decorator, chunkPos, type)).findFirst().orElse(vanillaAdapter);
    }

    public static int trySurface(WorldServer worldIn, Random random, BlockPos chunkPos, StructureSelector<VanillaDecorationGeneration, DecorationType> selector, DecorationType type, double totalWeight, double baseWeight, int vanillaAmount, boolean mayGiveUp)
    {
        int rcAmount = amount(random, totalWeight * baseWeight, vanillaAmount);

        // When the chance is low, we don't give back to vanilla to try once again, to avoid double the spawn rate
        if (rcAmount <= 0 && mayGiveUp) return -1;

        for (int i = 0; i < rcAmount; i++)
        {
            for (int t = 0; t < STRUCTURE_TRIES; t++)
               if (generateSurface(selector.selectOne(random, type, totalWeight), worldIn, chunkPos, random))
                break;
        }

        return vanillaAmount - rcAmount;
    }

    public static boolean generateSurface(Pair<Structure<?>, VanillaDecorationGeneration> generation, WorldServer worldIn, BlockPos chunkPos, Random random)
    {
        return new StructureGenerator<>(generation.getLeft()).generationInfo(generation.getRight()).world(worldIn)
                .random(random).maturity(StructureSpawnContext.GenerateMaturity.SUGGEST)
                .memorize(RCConfig.memorizeDecoration).allowOverlaps(true)
                .randomPosition(randomSurfacePos(random, chunkPos.add(generation.getRight().spawnShift)), // Shift +1 because surface placer goes -1
                        shift(generation.getRight().placer(), generation.getRight().spawnShift.getY() + 1)).fromCenter(true)
                .generate().isPresent();
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
        boolean mayGiveUp(WorldServer worldIn, Random random, Biome biome, BiomeDecorator decorator, BlockPos chunkPos, DecorationType type);

        /**
         * Generate one thing in the biome
         */
        void generate(WorldServer worldIn, Random random, Biome biome, BiomeDecorator decorator, BlockPos chunkPos, DecorationType type);
    }
}
