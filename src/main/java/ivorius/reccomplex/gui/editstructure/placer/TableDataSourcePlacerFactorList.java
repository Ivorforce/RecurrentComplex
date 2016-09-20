/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.placer;

import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedList;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.placement.FactorRegistry;
import ivorius.reccomplex.structures.generic.placement.GenericPlacer;

import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourcePlacerFactorList extends TableDataSourceList<GenericPlacer.Factor, List<GenericPlacer.Factor>>
{
    public TableDataSourcePlacerFactorList(List<GenericPlacer.Factor> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
        setUsesPresetActionForAdding(true);
    }

    @Override
    public String getDisplayString(GenericPlacer.Factor entry)
    {
        return entry.displayString();
    }

    @Override
    public GenericPlacer.Factor newEntry(String actionID)
    {
        return tryInstantiate(actionID, FactorRegistry.INSTANCE.getTypeRegistry().typeForID(actionID), "Failed instantiating placer factor: %s");
    }

    @Override
    public TableDataSource editEntryDataSource(GenericPlacer.Factor entry)
    {
        return entry.tableDataSource(navigator, tableDelegate);
    }

    @Override
    public TableCellButton[] getAddActions()
    {
        return TableDataSourcePresettedList.addActions(FactorRegistry.INSTANCE.getTypeRegistry().allIDs(), "reccomplex.placer.factors.", canEditList());
    }
}
