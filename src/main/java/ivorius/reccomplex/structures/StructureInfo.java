/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures;

import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.structures.generic.SavedMazeComponent;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;
import ivorius.reccomplex.structures.generic.gentypes.VanillaStructureSpawnInfo;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.List;
import java.util.Random;

/**
 * Created by lukas on 24.05.14.
 */
public interface StructureInfo
{
    void generate(StructureSpawnContext context);

    <I> List<I> generationInfos(Class<I> clazz);

    int[] structureBoundingBox();

    boolean isRotatable();

    boolean isMirrorable();

    GenericStructureInfo copyAsGenericStructureInfo();

    boolean areDependenciesResolved();
}
