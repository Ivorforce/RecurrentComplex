/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.biome;

import net.minecraft.init.Biomes;
import net.minecraftforge.common.BiomeDictionary;

/**
 * Created by lukas on 18.09.16.
 */
public class RCBiomeDictionary
{
    private static BiomeDictionary.Type BIRCH_TREES;
    private static BiomeDictionary.Type ROOFED_TREES;
    private static BiomeDictionary.Type THEMED_TREES;

    public static void registerTypes()
    {
        BIRCH_TREES = BiomeDictionary.Type.getType("BIRCH_TREES");
        ROOFED_TREES = BiomeDictionary.Type.getType("ROOFED_TREES");
        THEMED_TREES = BiomeDictionary.Type.getType("THEMED_TREES", BIRCH_TREES, ROOFED_TREES);

        BiomeDictionary.registerBiomeType(Biomes.BIRCH_FOREST, BIRCH_TREES);
        BiomeDictionary.registerBiomeType(Biomes.BIRCH_FOREST_HILLS, BIRCH_TREES);
        BiomeDictionary.registerBiomeType(Biomes.MUTATED_BIRCH_FOREST, BIRCH_TREES);
        BiomeDictionary.registerBiomeType(Biomes.MUTATED_BIRCH_FOREST_HILLS, BIRCH_TREES);

        BiomeDictionary.registerBiomeType(Biomes.ROOFED_FOREST, BIRCH_TREES);
    }
}
