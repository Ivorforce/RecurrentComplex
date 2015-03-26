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
public class TableDataSourceSpawnCommandEntry extends TableDataSourceSegmented implements TableElementPropertyListener, TableElementActionListener
{
    private TileEntitySpawnCommand.Entry entry;

    private TableDelegate tableDelegate;

    public TableDataSourceSpawnCommandEntry(TileEntitySpawnCommand.Entry entry, TableDelegate tableDelegate)
    {
        this.entry = entry;
        this.tableDelegate = tableDelegate;
    }

    @Override
    public int numberOfSegments()
    {
        return 1;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return 3;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
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
            TableElementFloatNullable element = new TableElementFloatNullable("weight", "Weight", TableElements.toFloat(entry.weight), 1.0f, 0, 10, "D", "C");
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
            entry.weight = TableElements.toDouble((Float) element.getPropertyValue());
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
