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
    private static BiomeDictionary.Type SUPER_BIRCH_TREES;
    private static BiomeDictionary.Type ROOFED_TREES;
    private static BiomeDictionary.Type MEGA_TAIGA_TREES;
    private static BiomeDictionary.Type MEGA_TAIGA_SPRUCE_TREES;
    private static BiomeDictionary.Type THEMED_TREES;

    public static void registerTypes()
    {
        BIRCH_TREES = BiomeDictionary.Type.getType("BIRCH_TREES");
        SUPER_BIRCH_TREES = BiomeDictionary.Type.getType("SUPER_BIRCH_TREES");
        ROOFED_TREES = BiomeDictionary.Type.getType("ROOFED_TREES");
        MEGA_TAIGA_TREES = BiomeDictionary.Type.getType("MEGA_TAIGA_TREES");
        MEGA_TAIGA_SPRUCE_TREES = BiomeDictionary.Type.getType("MEGA_TAIGA_SPRUCE_TREES");
        THEMED_TREES = BiomeDictionary.Type.getType("THEMED_TREES",
                BIRCH_TREES, SUPER_BIRCH_TREES, ROOFED_TREES,
                MEGA_TAIGA_TREES, MEGA_TAIGA_SPRUCE_TREES
        );

        BiomeDictionary.addTypes(Biomes.BIRCH_FOREST, BIRCH_TREES);
        BiomeDictionary.addTypes(Biomes.BIRCH_FOREST_HILLS, BIRCH_TREES);
        BiomeDictionary.addTypes(Biomes.MUTATED_BIRCH_FOREST, SUPER_BIRCH_TREES);
        BiomeDictionary.addTypes(Biomes.MUTATED_BIRCH_FOREST_HILLS, SUPER_BIRCH_TREES);

        BiomeDictionary.addTypes(Biomes.ROOFED_FOREST, BIRCH_TREES);

        BiomeDictionary.addTypes(Biomes.REDWOOD_TAIGA, MEGA_TAIGA_TREES);
        BiomeDictionary.addTypes(Biomes.REDWOOD_TAIGA_HILLS, MEGA_TAIGA_TREES);
        BiomeDictionary.addTypes(Biomes.MUTATED_REDWOOD_TAIGA, MEGA_TAIGA_SPRUCE_TREES);
        BiomeDictionary.addTypes(Biomes.MUTATED_REDWOOD_TAIGA_HILLS, MEGA_TAIGA_SPRUCE_TREES);
    }
}
