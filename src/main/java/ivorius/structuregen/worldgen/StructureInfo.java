package ivorius.structuregen.worldgen;

import ivorius.structuregen.ivtoolkit.AxisAlignedTransform2D;
import ivorius.structuregen.worldgen.genericStructures.GenericStructureInfo;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.Random;

/**
 * Created by lukas on 24.05.14.
 */
public interface StructureInfo
{
    void generate(World world, Random random, int x, int y, int z, boolean asCenter, int layer);

    void generateSource(World world, Random random, int x, int y, int z, int layer);

    int generationY(World world, Random random, int x, int z);

    int generationWeightInBiome(BiomeGenBase biome);

    String generationCategory();

    GenericStructureInfo copyAsGenericStructureInfo();

    boolean areDependenciesResolved();
}
