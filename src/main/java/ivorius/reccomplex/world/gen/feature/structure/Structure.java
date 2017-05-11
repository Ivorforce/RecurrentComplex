/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.world.gen.feature.structure;

import ivorius.ivtoolkit.blocks.IvBlockCollection;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericVariableDomain;
import ivorius.reccomplex.world.gen.feature.structure.generic.generation.GenerationType;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.TransformerMulti;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.nbt.NBTBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by lukas on 24.05.14.
 */
public interface Structure<S extends NBTStorable>
{
    void generate(@Nonnull StructureSpawnContext context, @Nonnull S instanceData, @Nonnull TransformerMulti transformer);

    @Nonnull
    S prepareInstanceData(@Nonnull StructurePrepareContext context, @Nonnull TransformerMulti transformer);

    @Nonnull
    S loadInstanceData(@Nonnull StructureLoadContext context, @Nonnull NBTBase nbt, @Nonnull TransformerMulti transformer);

    @Nonnull
    <I extends GenerationType> List<I> generationInfos(@Nonnull Class<? extends I> clazz);

    GenerationType generationInfo(@Nonnull String id);

    @Nonnull
    int[] size();

    boolean isRotatable();

    boolean isMirrorable();

    boolean isBlocking();

    @Nullable
    GenericStructure copyAsGenericStructureInfo();

    boolean areDependenciesResolved();

    @Nullable
    IvBlockCollection blockCollection();

    @Nonnull
    GenericVariableDomain declaredVariables();
}
