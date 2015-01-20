/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen;

import ivorius.reccomplex.worldgen.genericStructures.GenericStructureInfo;
import ivorius.reccomplex.worldgen.genericStructures.SavedMazeComponent;
import ivorius.reccomplex.worldgen.genericStructures.gentypes.VanillaStructureSpawnInfo;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.Random;

/**
 * Created by lukas on 24.05.14.
 */
public interface StructureInfo
{
    void generate(StructureSpawnContext context);

    int generationY(World world, Random random, int x, int z);

    int[] structureBoundingBox();

    boolean isRotatable();

    boolean isMirrorable();

    int generationWeightInBiome(BiomeGenBase biome);

    String generationCategory();

    GenericStructureInfo copyAsGenericStructureInfo();

    boolean areDependenciesResolved();

    String mazeID();

    SavedMazeComponent mazeComponent();

    VanillaStructureSpawnInfo vanillaStructureSpawnInfo();
}
