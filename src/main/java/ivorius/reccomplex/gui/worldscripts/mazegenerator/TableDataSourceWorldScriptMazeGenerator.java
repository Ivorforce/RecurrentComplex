/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.RCGuiTables;
import ivorius.reccomplex.gui.TableDataSourceBlockCoord;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.rules.TableDataSourceMazeRuleList;
import ivorius.reccomplex.scripts.world.WorldScriptMazeGenerator;
import ivorius.reccomplex.structures.generic.maze.*;
import ivorius.reccomplex.structures.generic.maze.rules.saved.MazeRuleConnectAll;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceWorldScriptMazeGenerator extends TableDataSourceSegmented implements TableCellPropertyListener, TableCellActionListener
{
    public static final int[] DIMENSIONS = new int[]{100, 100, 100};
    private WorldScriptMazeGenerator script;

    private TableDelegate tableDelegate;
    private TableNavigator tableNavigator;

    public TableDataSourceWorldScriptMazeGenerator(WorldScriptMazeGenerator script, TableDelegate tableDelegate, TableNavigator tableNavigator)
    {
        this.script = script;
        this.tableDelegate = tableDelegate;
        this.tableNavigator = tableNavigator;

        addManagedSection(2, new TableDataSourceBlockCoord(script.getStructureShift(), script::setStructureShift, new IntegerRange(-50, 50), "Range: %s"));
    }

    public WorldScriptMazeGenerator getScript()
    {
        return script;
    }

    public void setScript(WorldScriptMazeGenerator script)
    {
        this.script = script;
    }

    public TableDelegate getTableDelegate()
    {
        return tableDelegate;
    }

    public void setTableDelegate(TableDelegate tableDelegate)
    {
        this.tableDelegate = tableDelegate;
    }

    public TableNavigator getTableNavigator()
    {
        return tableNavigator;
    }

    public void setTableNavigator(TableNavigator tableNavigator)
    {
        this.tableNavigator = tableNavigator;
    }

    @Override
    public int numberOfSegments()
    {
        return 4;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 0:
                return 1;
            case 1:
            case 3:
                return 3;
            default:
                return super.sizeOfSegment(segment);
        }
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableCellString cell = new TableCellString("mazeID", script.getMazeID());
            cell.addPropertyListener(this);
            return new TableElementCell("Maze ID", cell);
        }
        else if (segment == 1)
        {
            if (index == 0)
            {
                TableCellButton cell = new TableCellButton("rooms", new TableCellButton.Action("edit", "Edit"));
                cell.addListener(this);
                return new TableElementCell("Rooms", cell);
            }
            else if (index == 1)
            {
                TableCellButton cell = new TableCellButton("exits", new TableCellButton.Action("edit", "Edit"));
                cell.addListener(this);
                return new TableElementCell("Exits", cell);
            }
            else if (index == 2)
            {
                TableCellButton cell = new TableCellButton("rules", new TableCellButton.Action("edit", "Edit"));
                cell.addListener(this);
                return new TableElementCell("Rules", cell);
            }
        }
        else if (segment == 3)
        {
            if (index == 0)
            {
                TableCellInteger cell = new TableCellInteger("roomSizeX", script.getRoomSize()[0], 1, 64);
                cell.addPropertyListener(this);
                return new TableElementCell("Room Size: X", cell);
            }
            else if (index == 1)
            {
                TableCellInteger cell = new TableCellInteger("roomSizeY", script.getRoomSize()[1], 1, 64);
                cell.addPropertyListener(this);
                return new TableElementCell("Room Size: Y", cell);
            }
            else if (index == 2)
            {
                TableCellInteger cell = new TableCellInteger("roomSizeZ", script.getRoomSize()[2], 1, 64);
                cell.addPropertyListener(this);
                return new TableElementCell("Room Size: Z", cell);
            }
        }

        return null;
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if ("mazeID".equals(cell.getID()))
        {
            script.setMazeID((String) cell.getPropertyValue());
        }
        else if ("roomSizeX".equals(cell.getID()))
        {
            int[] size = script.getRoomSize();
            size[0] = (int) cell.getPropertyValue();
            script.setRoomSize(size);
        }
        else if ("roomSizeY".equals(cell.getID()))
        {
            int[] size = script.getRoomSize();
            size[1] = (int) cell.getPropertyValue();
            script.setRoomSize(size);
        }
        else if ("roomSizeZ".equals(cell.getID()))
        {
            int[] size = script.getRoomSize();
            size[2] = (int) cell.getPropertyValue();
            script.setRoomSize(size);
        }
    }

    @Override
    public void actionPerformed(TableCell tableElementButton, String actionID)
    {
        if ("exits".equals(tableElementButton.getID()))
        {
            tableNavigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazePathConnectionList(script.exitPaths, tableDelegate, tableNavigator, script.rooms.boundsLower(), script.rooms.boundsHigher())));
        }
        else if ("rooms".equals(tableElementButton.getID()))
        {
            tableNavigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceSelection(script.rooms, DIMENSIONS, tableDelegate, tableNavigator)));
        }
        else if ("rules".equals(tableElementButton.getID()))
        {
            tableNavigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazeRuleList(script.rules, tableDelegate, tableNavigator, script.exitPaths, script.rooms.boundsLower(), script.rooms.boundsHigher())));
        }
    }
}
