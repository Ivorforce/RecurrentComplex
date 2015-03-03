/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.structures.generic.blocktransformers;

import ivorius.ivtoolkit.tools.IvWorldData;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.structures.StructureSpawnContext;
import net.minecraft.block.Block;

import java.util.List;

/**
 * Created by lukas on 25.05.14.
 */
public interface BlockTransformer
{
    boolean skipGeneration(Block block, int metadata);

    void transform(Phase phase, StructureSpawnContext context, IvWorldData worldData, List<BlockTransformer> transformerList);

    boolean generatesInPhase(Phase phase);

    String getDisplayString();

    public TableDataSource tableDataSource(TableNavigator navigator, TableDelegate delegate);

    public static enum Phase
    {
        BEFORE,
        AFTER
    }
}
