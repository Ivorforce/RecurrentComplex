/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.world.gen.script;

import net.minecraft.util.math.BlockPos;
import ivorius.ivtoolkit.tools.NBTCompoundObject;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.world.gen.feature.structure.StructureLoadContext;
import ivorius.reccomplex.world.gen.feature.structure.StructurePrepareContext;
import ivorius.reccomplex.world.gen.feature.structure.StructureSpawnContext;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.nbt.NBTBase;

/**
 * Created by lukas on 13.09.15.
 */
public interface WorldScript<S extends NBTStorable> extends NBTCompoundObject
{
    S prepareInstanceData(StructurePrepareContext context, BlockPos pos);

    S loadInstanceData(StructureLoadContext context, NBTBase nbt);

    void generate(StructureSpawnContext context, S instanceData, BlockPos coord);

    String getDisplayString();

    TableDataSource tableDataSource(TableNavigator navigator, TableDelegate tableDelegate);
}
