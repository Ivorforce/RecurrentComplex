/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.placer;

import ivorius.reccomplex.gui.editstructure.preset.TableDataSourcePresettedList;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.FactorLimit;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceLimitRayList extends TableDataSourceList<FactorLimit.Ray, List<FactorLimit.Ray>>
{
    public TableDataSourceLimitRayList(List<FactorLimit.Ray> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
        setUsesPresetActionForAdding(true);
    }

    @Override
    public String getDisplayString(FactorLimit.Ray entry)
    {
        return entry.displayString();
    }

    @Override
    public FactorLimit.Ray newEntry(String actionID)
    {
        return tryInstantiate(actionID, FactorLimit.getRayRegistry().typeForID(actionID), "Failed instantiating limit factor ray: %s");
    }

    @Nonnull
    @Override
    public TableCell entryCell(boolean enabled, FactorLimit.Ray ray)
    {
        return editCell(enabled, navigator, tableDelegate, () -> ray.tableDataSource(navigator, tableDelegate));
    }

    @Override
    public List<TableCellButton> getAddActions()
    {
        return TableDataSourcePresettedList.addActions(FactorLimit.getRayRegistry().allIDs(), "reccomplex.placer.factors.limit.rays.", canEditList());
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Rays";
    }
}
