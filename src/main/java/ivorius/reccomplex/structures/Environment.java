/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures;

import ivorius.reccomplex.worldgen.StructureGenerator;
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

    public Environment(@Nonnull WorldServer world, @Nonnull Biome biome, @Nullable Integer villageType)
    {
        this.world = world;
        this.biome = biome;
        this.villageType = villageType;
    }

    @Nonnull
    public static Environment inNature(@Nonnull WorldServer world, @Nonnull StructureBoundingBox boundingBox)
    {
        return new Environment(world, StructureGenerator.getBiome(world, boundingBox), null);
    }
}
