/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure;

import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructureInfo;
import ivorius.reccomplex.world.gen.feature.structure.generic.gentypes.GenerationInfo;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.TransformerMulti;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.nbt.NBTBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 24.05.14.
 */
public interface StructureInfo<S extends NBTStorable>
{
    boolean generate(@Nonnull StructureSpawnContext context, @Nonnull S instanceData, @Nonnull TransformerMulti transformer);

    @Nonnull
    S prepareInstanceData(@Nonnull StructurePrepareContext context, @Nonnull TransformerMulti transformer);

    @Nonnull
    S loadInstanceData(@Nonnull StructureLoadContext context, @Nonnull NBTBase nbt, @Nonnull TransformerMulti transformer);

    @Nonnull
    <I extends GenerationInfo> List<I> generationInfos(@Nonnull Class<? extends I> clazz);

    GenerationInfo generationInfo(@Nonnull String id);

    @Nonnull
    int[] size();

    boolean isRotatable();

    boolean isMirrorable();

    boolean isBlocking();

    @Nullable
    GenericStructureInfo copyAsGenericStructureInfo();

    boolean areDependenciesResolved();

    @Nullable
    IvBlockCollection blockCollection();
}
