/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures;

import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by lukas on 08.09.16.
 */
public class Environment
{
    @Nonnull
    public final WorldServer world;
    @Nonnull
    public final Biome biome;
    @Nullable
    public final Integer villageType;
    @Nullable
    public final StructureGenerationInfo generationInfo;

    public Environment(@Nonnull WorldServer world, @Nonnull Biome biome, @Nullable Integer villageType, @Nullable StructureGenerationInfo generationInfo)
    {
        this.world = world;
        this.biome = biome;
        this.villageType = villageType;
        this.generationInfo = generationInfo;
    }

    @Nonnull
    public static Environment inNature(@Nonnull WorldServer world, @Nonnull StructureBoundingBox boundingBox, StructureGenerationInfo generationInfo)
    {
        return new Environment(world, getBiome(world, boundingBox), null, generationInfo);
    }

    @Nonnull
    public static Environment inNature(@Nonnull WorldServer world, @Nonnull StructureBoundingBox boundingBox)
    {
        return inNature(world, boundingBox, null);
    }

    public static Biome getBiome(World world, StructureBoundingBox boundingBox)
    {
        return world.getBiome(new BlockPos(boundingBox.getCenter()));
    }

    public Environment withGeneration(StructureGenerationInfo generation)
    {
        return new Environment(world, biome, villageType, generationInfo);
    }
}
