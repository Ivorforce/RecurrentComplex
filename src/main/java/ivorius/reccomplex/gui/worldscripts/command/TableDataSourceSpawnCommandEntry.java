/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.command;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.scripts.world.WorldScriptCommand;
import net.minecraft.init.Blocks;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceSpawnCommandEntry extends TableDataSourceSegmented implements TableCellPropertyListener, TableCellActionListener
{
    private WorldScriptCommand.Entry entry;

    private TableDelegate tableDelegate;

    public TableDataSourceSpawnCommandEntry(WorldScriptCommand.Entry entry, TableDelegate tableDelegate)
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
            TableCellPresetAction cell = new TableCellPresetAction("default", IvTranslations.get("reccomplex.gui.apply"),
                    new TableCellButton("", "spawner", Blocks.MOB_SPAWNER.getLocalizedName()),
                    new TableCellButton("", "entity", IvTranslations.get("reccomplex.spawncommand.preset.entity"))
            );
            cell.addListener(this);
            return new TableElementCell(IvTranslations.get("reccomplex.gui.preset"), cell);
        }
        else if (index == 1)
        {
            TableCellString cell = new TableCellString("command", entry.command);
            cell.setMaxStringLength(32767); // Same as GuiCommandBlock.
            cell.addPropertyListener(this);
            return new TableElementCell(IvTranslations.get("reccomplex.gui.command"), cell);
        }
        else if (index == 2)
        {
            return RCGuiTables.defaultWeightElement(cell -> entry.weight = TableElements.toDouble((Float) cell.getPropertyValue()), entry.weight);
        }

        return null;
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if ("command".equals(cell.getID()))
        {
            entry.command = (String) cell.getPropertyValue();
        }
    }

    @Override
    public void actionPerformed(TableCell cell, String actionID)
    {
        if ("default".equals(cell.getID()))
        {
            if ("spawner".equals(actionID))
                entry.command = "/setblock ~ ~ ~ mob_spawner 0 replace {EntityId:Zombie}";
            else if ("entity".equals(actionID))
                entry.command = "/summon Zombie ~ ~ ~";

            tableDelegate.reloadData();
        }
    }
}
