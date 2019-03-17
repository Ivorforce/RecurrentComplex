/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.block;

import ivorius.reccomplex.world.gen.feature.structure.context.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.transformers.RunTransformer;
import net.minecraft.nbt.NBTBase;

/**
 * Created by lukas on 06.06.14.
 */
public interface GeneratingTileEntity<S>
{
    S prepareInstanceData(StructurePrepareContext context);

    S loadInstanceData(StructureLoadContext context, NBTBase nbt);

    void generate(StructureSpawnContext context, RunTransformer transformer, S instanceData);

    default boolean shouldPlaceInWorld(StructurePrepareContext context, S instanceData) {
        return false;
    }
}
