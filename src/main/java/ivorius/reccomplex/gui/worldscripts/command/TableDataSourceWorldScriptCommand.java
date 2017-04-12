/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.command;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.worldscripts.TableDataSourceWorldScript;
import ivorius.reccomplex.utils.RCStrings;
import ivorius.reccomplex.world.gen.script.WorldScriptCommand;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceWorldScriptCommand extends TableDataSourceSegmented
{
    public TableDataSourceWorldScriptCommand(WorldScriptCommand script, TableDelegate tableDelegate, TableNavigator navigator)
    {
        addManagedSegment(0, new TableDataSourceWorldScript(script));

        addManagedSegment(1, new TableDataSourceList<WorldScriptCommand.Entry, List<WorldScriptCommand.Entry>>(script.entries, tableDelegate, navigator){
            @Override
            public String getDisplayString(WorldScriptCommand.Entry entry)
            {
                return RCStrings.abbreviateFormatted(entry.command, 20);
            }

            @Override
            public WorldScriptCommand.Entry newEntry(String actionID)
            {
                return new WorldScriptCommand.Entry(1.0, "");
            }

            @Nonnull
            @Override
            public TableCell entryCell(boolean enabled, WorldScriptCommand.Entry entry)
            {
                return TableCells.edit(enabled, navigator, tableDelegate, () -> new TableDataSourceSpawnCommandEntry(entry, tableDelegate));
            }
        });
    }
}
