/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.command;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceList;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.worldscripts.TableDataSourceWorldScript;
import ivorius.reccomplex.world.gen.script.WorldScriptCommand;
import org.apache.commons.lang3.StringUtils;

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
                return StringUtils.abbreviate(entry.command, 20);
            }

            @Override
            public WorldScriptCommand.Entry newEntry(String actionID)
            {
                return new WorldScriptCommand.Entry(1.0, "");
            }

            @Override
            public TableDataSource editEntryDataSource(WorldScriptCommand.Entry entry)
            {
                return new TableDataSourceSpawnCommandEntry(entry, tableDelegate);
            }
        });
    }
}
