/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.command;

import ivorius.reccomplex.blocks.TileEntitySpawnCommand;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.scripts.world.WorldScriptCommand;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceWorldScriptCommand extends TableDataSourceList<WorldScriptCommand.Entry, List<WorldScriptCommand.Entry>>
{
    public TableDataSourceWorldScriptCommand(WorldScriptCommand script, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(script.entries, tableDelegate, navigator);
        setEarlierTitle("Up");
        setLaterTitle("Down");
    }

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
}
