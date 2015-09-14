/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editmazeblock;

import ivorius.ivtoolkit.blocks.BlockCoord;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.scripts.world.WorldScriptMazeGenerator;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceMazeBlock extends TableDataSourceSegmented implements TableCellPropertyListener, TableCellActionListener
{
    public static final int[] DIMENSIONS = new int[]{100, 100, 100};
    private WorldScriptMazeGenerator script;

    private TableDelegate tableDelegate;
    private TableNavigator tableNavigator;

    public TableDataSourceMazeBlock(WorldScriptMazeGenerator script, TableDelegate tableDelegate, TableNavigator tableNavigator)
    {
        this.script = script;
        this.tableDelegate = tableDelegate;
        this.tableNavigator = tableNavigator;
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
        return segment == 0 ? 1 : segment == 1 ? 2 : 3;
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
        }
        else if (segment == 2)
        {
            if (index == 0)
            {
                TableCellInteger cell = new TableCellInteger("xShift", script.getStructureShift().x, -50, 50);
                cell.addPropertyListener(this);
                return new TableElementCell("Shift: X", cell);
            }
            else if (index == 1)
            {
                TableCellInteger cell = new TableCellInteger("yShift", script.getStructureShift().y, -50, 50);
                cell.addPropertyListener(this);
                return new TableElementCell("Shift: Y", cell);
            }
            else if (index == 2)
            {
                TableCellInteger cell = new TableCellInteger("zShift", script.getStructureShift().z, -50, 50);
                cell.addPropertyListener(this);
                return new TableElementCell("Shift: Z", cell);
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
        else if ("xShift".equals(cell.getID()))
        {
            BlockCoord shift = script.getStructureShift();
            script.setStructureShift(new BlockCoord((int) cell.getPropertyValue(), shift.y, shift.z));
        }
        else if ("yShift".equals(cell.getID()))
        {
            BlockCoord shift = script.getStructureShift();
            script.setStructureShift(new BlockCoord(shift.x, (int) cell.getPropertyValue(), shift.z));
        }
        else if ("zShift".equals(cell.getID()))
        {
            BlockCoord shift = script.getStructureShift();
            script.setStructureShift(new BlockCoord(shift.x, shift.y, (int) cell.getPropertyValue()));
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
            tableNavigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazePathList(script.mazeExits, tableDelegate, tableNavigator, script.mazeRooms.boundsLower(), script.mazeRooms.boundsHigher())));
        }
        else if ("rooms".equals(tableElementButton.getID()))
        {
            tableNavigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceSelection(script.mazeRooms, DIMENSIONS, tableDelegate, tableNavigator)));
        }
    }
}
