/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editspawncommandblock;

import ivorius.reccomplex.blocks.TileEntitySpawnCommand;
import ivorius.reccomplex.gui.table.*;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceSpawnCommandEntry implements TableDataSource, TableElementPropertyListener, TableElementPresetAction.Listener
{
    private TileEntitySpawnCommand.Entry entry;

    private TableDelegate tableDelegate;

    public TableDataSourceSpawnCommandEntry(TileEntitySpawnCommand.Entry entry, TableDelegate tableDelegate)
    {
        this.entry = entry;
        this.tableDelegate = tableDelegate;
    }

    @Override
    public boolean has(GuiTable table, int index)
    {
        return index >= 0 && index < 3;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        if (index == 0)
        {
            TableElementPresetAction element = new TableElementPresetAction("default", "Preset", "Apply", new TableElementButton.Action("spawner", "Mob Spawner"), new TableElementButton.Action("entity", "Spawn Entity"));
            element.addListener(this);
            return element;
        }
        else if (index == 1)
        {
            TableElementString element = new TableElementString("command", "Command", entry.command);
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 2)
        {
            TableElementInteger element = new TableElementInteger("weight", "Weight", entry.itemWeight, 0, 500);
            element.addPropertyListener(this);
            return element;
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("command".equals(element.getID()))
        {
            entry.command = (String) element.getPropertyValue();
        }
        else if ("weight".equals(element.getID()))
        {
            entry.itemWeight = (Integer) element.getPropertyValue();
        }
    }

    @Override
    public void actionPerformed(TableElementPresetAction tableElementButton, String actionID)
    {
        if ("default".equals(tableElementButton.getID()))
        {
            if ("spawner".equals(actionID))
                entry.command = "/setblock ~ ~ ~ mob_spawner 0 replace {EntityId:Zombie}";
            else if ("entity".equals(actionID))
                entry.command = "/summon Zombie ~ ~ ~";

            tableDelegate.reloadData();
        }
    }
}
