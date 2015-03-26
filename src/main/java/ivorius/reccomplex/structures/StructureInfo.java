/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures;

import ivorius.reccomplex.structures.generic.GenericStructureInfo;
import ivorius.reccomplex.structures.generic.gentypes.StructureGenerationInfo;

import java.util.List;

/**
 * Created by lukas on 24.05.14.
 */
public interface StructureInfo
{
    void generate(StructureSpawnContext context);

    <I extends StructureGenerationInfo> List<I> generationInfos(Class<I> clazz);

    StructureGenerationInfo generationInfo(String id);

    int[] structureBoundingBox();

    boolean isRotatable();

    boolean isMirrorable();

    GenericStructureInfo copyAsGenericStructureInfo();

    boolean areDependenciesResolved();
}
