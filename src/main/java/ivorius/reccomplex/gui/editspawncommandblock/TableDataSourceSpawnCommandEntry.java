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
public class TableDataSourceSpawnCommandEntry implements TableDataSource, TableElementPropertyListener, TableElementActionListener
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
        return index >= 0 && index < 4;
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
            TableElementBoolean element = new TableElementBoolean("defaultWeight", "Use Default Weight", entry.hasDefaultWeight());
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 3)
        {
            TableElementFloat element = new TableElementFloat("weight", "Weight", (float) entry.getWeight(), 0, 10);
            element.setEnabled(!entry.hasDefaultWeight());
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
            entry.weight = (double) (Float) element.getPropertyValue();
        }
        else if ("defaultWeight".equals(element.getID()))
        {
            boolean useDefault = (boolean) element.getPropertyValue();
            entry.weight = useDefault ? null : entry.getWeight();
            tableDelegate.reloadData();
        }
    }

    @Override
    public void actionPerformed(TableElement element, String actionID)
    {
        if ("default".equals(element.getID()))
        {
            if ("spawner".equals(actionID))
                entry.command = "/setblock ~ ~ ~ mob_spawner 0 replace {EntityId:Zombie}";
            else if ("entity".equals(actionID))
                entry.command = "/summon Zombie ~ ~ ~";

            tableDelegate.reloadData();
        }
    }
}
