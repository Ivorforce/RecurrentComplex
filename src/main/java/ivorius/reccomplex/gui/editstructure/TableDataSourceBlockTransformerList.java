/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.StructureRegistry;
import ivorius.reccomplex.structures.generic.blocktransformers.BlockTransformer;
import net.minecraft.util.StatCollector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceBlockTransformerList extends TableDataSourceList<BlockTransformer, List<BlockTransformer>>
{
    public TableDataSourceBlockTransformerList(List<BlockTransformer> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
        setUsesPresetActionForAdding(true);
    }

    @Override
    public String getDisplayString(BlockTransformer blockTransformer)
    {
        return blockTransformer.displayString();
    }

    @Override
    public BlockTransformer newEntry(String actionID)
    {
        Class<? extends BlockTransformer> clazz = StructureRegistry.getBlockTransformerRegistry().typeForID(actionID);

        return instantiateBlockTransformer(clazz);
    }

    @Override
    public TableDataSource editEntryDataSource(BlockTransformer entry)
    {
        return entry.tableDataSource(navigator, tableDelegate);
    }

    @Override
    public TableElementButton.Action[] getAddActions()
    {
        Collection<String> allTypes = StructureRegistry.getBlockTransformerRegistry().allIDs();
        List<TableElementButton.Action> actions = new ArrayList<>(allTypes.size());
        for (String type : allTypes)
            actions.add(new TableElementButton.Action(type, StatCollector.translateToLocal("reccomplex.blockTransformer." + type)));
        return actions.toArray(new TableElementButton.Action[actions.size()]);
    }

    public BlockTransformer instantiateBlockTransformer(Class<? extends BlockTransformer> clazz)
    {
        BlockTransformer blockTransformer = null;

        try
        {
            blockTransformer = clazz.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            RecurrentComplex.logger.error(e);
        }

        return blockTransformer;
    }
}
