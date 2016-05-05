/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.transformers;

import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.structures.StructureLoadContext;
import ivorius.reccomplex.structures.StructurePrepareContext;
import ivorius.reccomplex.structures.StructureSpawnContext;
import ivorius.reccomplex.utils.BlockState;
import ivorius.reccomplex.utils.NBTStorable;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTBase;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public interface Transformer<S extends NBTStorable>
{
    String getDisplayString();

    TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate);

    S prepareInstanceData(StructurePrepareContext context);

    S loadInstanceData(StructureLoadContext context, NBTBase nbt);

    boolean skipGeneration(S instanceData, BlockState state);

    void transform(S instanceData, Phase phase, StructureSpawnContext context, IvWorldData worldData, List<Pair<Transformer, NBTStorable>> transformers);

    boolean generatesInPhase(S instanceData, Phase phase);

    enum Phase
    {
        BEFORE,
        AFTER
    }
}
