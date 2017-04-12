/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.multi;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellButton;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.utils.RCStrings;
import ivorius.reccomplex.world.gen.script.WorldScript;
import ivorius.reccomplex.world.gen.script.WorldScriptRegistry;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceWorldScriptList extends TableDataSourceList<WorldScript, List<WorldScript>>
{
    public TableDataSourceWorldScriptList(List<WorldScript> list, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
        setUsesPresetActionForAdding(true);
    }

    @Override
    public String getDisplayString(WorldScript script)
    {
        return RCStrings.abbreviateFormatted(script.getDisplayString(), 24);
    }

    @Override
    public WorldScript newEntry(String actionID)
    {
        return tryInstantiate(actionID, WorldScriptRegistry.INSTANCE.objectClass(actionID), "Failed instantiating world script: %s");
    }

    @Nonnull
    @Override
    public TableCell entryCell(boolean enabled, WorldScript worldScript)
    {
        return TableCells.edit(enabled, navigator, tableDelegate, () -> worldScript.tableDataSource(navigator, tableDelegate));
    }

    @Override
    public List<TableCellButton> getAddActions()
    {
        return TableCells.addManyWithBase(WorldScriptRegistry.INSTANCE.allIDs(), "reccomplex.worldscript.", canEditList());
    }

    @Nonnull
    @Override
    public String title()
    {
        return "Scripts";
    }
}
