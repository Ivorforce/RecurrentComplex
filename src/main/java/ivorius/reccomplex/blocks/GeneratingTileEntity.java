/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.blocks;

import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import net.minecraft.nbt.NBTBase;

/**
 * Created by lukas on 06.06.14.
 */
public interface GeneratingTileEntity<S>
{
    S prepareInstanceData(StructurePrepareContext context);

    S loadInstanceData(StructureLoadContext context, NBTBase nbt);

    void generate(StructureSpawnContext context, S instanceData);

    boolean shouldPlaceInWorld(StructureSpawnContext context, S instanceData);
}
