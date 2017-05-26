/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.feature.decoration;

import ivorius.reccomplex.RecurrentComplex;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenDesertWells;
import net.minecraft.world.gen.feature.WorldGenFossils;

import java.util.Random;

/**
 * Created by lukas on 09.10.16.
 */
public class VanillaDecorationAdapter implements RCBiomeDecorator.Adapter
{
    @Override
    public int amount(WorldServer worldIn, Random random, Biome biomeIn, BiomeDecorator decorator, BlockPos chunkPos, RCBiomeDecorator.DecorationType type)
    {
        switch (type)
        {
            case TREE:
            {
                int vanillaAmount = decorator.treesPerChunk;

                if (random.nextFloat() < decorator.extraTreeChance)
                    ++vanillaAmount;

                return vanillaAmount;
            }
            case BIG_SHROOM:
                return decorator.bigMushroomsPerChunk;
            case CACTUS:
                return decorator.cactiPerChunk;
            case DESERT_WELL:
                return random.nextInt(100) == 0 ? 1 : 0;
            case FOSSIL:
                return random.nextInt(64) == 0 ? 1 : 0;
            default:
                throw new IllegalArgumentException("Unrecognized type: " + type.toString());
        }
    }

    @Override
    public boolean mayGiveUp(WorldServer worldIn, Random random, Biome biome, BiomeDecorator decorator, BlockPos chunkPos, RCBiomeDecorator.DecorationType type)
    {
        return !(type == RCBiomeDecorator.DecorationType.DESERT_WELL || type == RCBiomeDecorator.DecorationType.FOSSIL
                || type == RCBiomeDecorator.DecorationType.TREE && decorator.treesPerChunk == 0); // Low tree density type
    }

    @Override
    public boolean matches(WorldServer worldIn, Biome biome, BiomeDecorator decorator, BlockPos chunkPos, RCBiomeDecorator.DecorationType type)
    {
        return true; // I am teh default
    }

    @Override
    public void generate(WorldServer worldIn, Random random, Biome biomeIn, BiomeDecorator decorator, BlockPos chunkPos, RCBiomeDecorator.DecorationType type)
    {
        try
        {
            switch (type)
            {
                case TREE:
                {
                    int k6 = random.nextInt(16) + 8;
                    int l = random.nextInt(16) + 8;
                    WorldGenAbstractTree worldgenabstracttree = biomeIn.getRandomTreeFeature(random);
                    worldgenabstracttree.setDecorationDefaults();
                    BlockPos blockpos = worldIn.getHeight(chunkPos.add(k6, 0, l));

                    if (worldgenabstracttree.generate(worldIn, random, blockpos))
                        worldgenabstracttree.generateSaplings(worldIn, random, blockpos);

                    break;
                }
                case BIG_SHROOM:
                {
                    int l6 = random.nextInt(16) + 8;
                    int k10 = random.nextInt(16) + 8;
                    decorator.bigMushroomGen.generate(worldIn, random, worldIn.getHeight(chunkPos.add(l6, 0, k10)));

                    break;
                }
                case CACTUS:
                {
                    int l9 = random.nextInt(16) + 8;
                    int k13 = random.nextInt(16) + 8;
                    int l16 = worldIn.getHeight(chunkPos.add(l9, 0, k13)).getY() * 2;

                    if (l16 > 0)
                    {
                        int j19 = random.nextInt(l16);
                        decorator.cactusGen.generate(worldIn, random, chunkPos.add(l9, j19, k13));
                    }

                    break;
                }
                case DESERT_WELL:
                {
                    int i = random.nextInt(16) + 8;
                    int j = random.nextInt(16) + 8;
                    BlockPos blockpos = worldIn.getHeight(chunkPos.add(i, 0, j)).up();
                    (new WorldGenDesertWells()).generate(worldIn, random, blockpos);

                    break;
                }
                case FOSSIL:
                {
                    (new WorldGenFossils()).generate(worldIn, random, chunkPos);

                    break;
                }
                default:
                    throw new IllegalArgumentException("Unrecognized type: " + type.toString());
            }
        }
        catch (Exception e)
        {
            RecurrentComplex.logger.error("Error trying to emulate vanilla decoration", e);
        }
    }
}
