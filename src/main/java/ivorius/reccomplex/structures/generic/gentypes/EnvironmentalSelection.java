/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.structures.generic.gentypes;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;

/**
 * Created by lukas on 23.09.16.
 */
public interface EnvironmentalSelection<C>
{
    double getGenerationWeight(Biome biome, WorldProvider provider);

    C generationCategory();
}
