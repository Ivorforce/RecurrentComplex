/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.worldgen;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.worldgen.genericStructures.GenericStructureInfo;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.Random;

/**
 * Created by lukas on 24.05.14.
 */
public interface StructureInfo
{
    void generate(World world, Random random, BlockCoord coord, AxisAlignedTransform2D transform, int layer);

    void generateSource(World world, Random random, BlockCoord coord, int layer, AxisAlignedTransform2D transform);

    int generationY(World world, Random random, int x, int z);

    int[] structureBoundingBox();

    boolean isRotatable();

    boolean isMirrorable();

    int generationWeightInBiome(BiomeGenBase biome);

    String generationCategory();

    GenericStructureInfo copyAsGenericStructureInfo();

    boolean areDependenciesResolved();
}
