/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.placer;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.FactorRegistry;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.GenericPlacer;

import javax.annotation.Nonnull;
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
    public GenericPlacer.Factor newEntry(int addIndex, String actionID)
    {
        return tryInstantiate(actionID, FactorRegistry.INSTANCE.getTypeRegistry().typeForID(actionID), "Failed instantiating placer factor: %s");
    }

    @Nonnull
    @Override
    public TableCell entryCell(boolean enabled, GenericPlacer.Factor factor)
    {
        return TableCells.edit(enabled, navigator, tableDelegate, () -> factor.tableDataSource(navigator, tableDelegate));
    }

    @Override
    public List<TableCellButton> getAddActions()
    {
        return TableCells.addManyWithBase(FactorRegistry.INSTANCE.getTypeRegistry().allIDs(), "reccomplex.placer.factors.", canEditList());
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Placer Factors";
    }
}
