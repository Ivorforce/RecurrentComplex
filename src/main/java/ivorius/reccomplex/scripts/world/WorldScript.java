/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.scripts.world;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.nbt.NBTBase;
import net.minecraft.world.World;

/**
 * Created by lukas on 13.09.15.
 */
public interface WorldScript<S extends NBTStorable> extends NBTCompoundObject
{
    S prepareInstanceData(StructurePrepareContext context, BlockCoord coord, World world);

    S loadInstanceData(StructureLoadContext context, NBTBase nbt);

    void generate(StructureSpawnContext context, S instanceData, BlockCoord coord);
}
