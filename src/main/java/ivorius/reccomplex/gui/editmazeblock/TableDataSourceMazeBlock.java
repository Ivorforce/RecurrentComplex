/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editmazeblock;

import ivorius.reccomplex.blocks.TileEntityMazeGenerator;
import ivorius.reccomplex.gui.table.*;
import ivorius.ivtoolkit.blocks.BlockCoord;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceMazeBlock extends TableDataSourceSegmented implements TableElementPropertyListener, TableElementButton.Listener
{
    private TileEntityMazeGenerator mazeGenerator;

    private TableDelegate tableDelegate;
    private TableNavigator tableNavigator;

    public TableDataSourceMazeBlock(TileEntityMazeGenerator mazeGenerator, TableDelegate tableDelegate, TableNavigator tableNavigator)
    {
        this.mazeGenerator = mazeGenerator;
        this.tableDelegate = tableDelegate;
        this.tableNavigator = tableNavigator;
    }

    public TileEntityMazeGenerator getMazeGenerator()
    {
        return mazeGenerator;
    }

    public void setMazeGenerator(TileEntityMazeGenerator mazeGenerator)
    {
        this.mazeGenerator = mazeGenerator;
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
        return 5;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        if (segment == 0 || segment == 1)
        {
            return 1;
        }

        return 3;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableElementButton element = new TableElementButton("components", "Components", new TableElementButton.Action("edit", "Edit"));
            element.addListener(this);
            return element;
        }
        else if (segment == 1)
        {
            TableElementButton element = new TableElementButton("exits", "Exits", new TableElementButton.Action("edit", "Edit"));
            element.addListener(this);
            return element;
        }
        else if (segment == 2)
        {
            if (index == 0)
            {
                TableElementInteger element = new TableElementInteger("xShift", "Shift: X", mazeGenerator.getStructureShift().x, -50, 50);
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 1)
            {
                TableElementInteger element = new TableElementInteger("yShift", "Shift: Y", mazeGenerator.getStructureShift().y, -50, 50);
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 2)
            {
                TableElementInteger element = new TableElementInteger("zShift", "Shift: Z", mazeGenerator.getStructureShift().z, -50, 50);
                element.addPropertyListener(this);
                return element;
            }
        }
        else if (segment == 3)
        {
            if (index == 0)
            {
                TableElementInteger element = new TableElementInteger("roomSizeX", "Room Size: X", mazeGenerator.getRoomSize()[0], 1, 20);
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 1)
            {
                TableElementInteger element = new TableElementInteger("roomSizeY", "Room Size: Y", mazeGenerator.getRoomSize()[1], 1, 20);
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 2)
            {
                TableElementInteger element = new TableElementInteger("roomSizeZ", "Room Size: Z", mazeGenerator.getRoomSize()[2], 1, 20);
                element.addPropertyListener(this);
                return element;
            }
        }
        else if (segment == 4)
        {
            if (index == 0)
            {
                TableElementInteger element = new TableElementInteger("roomsX", "Rooms: X", mazeGenerator.getRoomNumbers()[0], 1, 20);
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 1)
            {
                TableElementInteger element = new TableElementInteger("roomsY", "Rooms: Y", mazeGenerator.getRoomNumbers()[1], 1, 20);
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 2)
            {
                TableElementInteger element = new TableElementInteger("roomsZ", "Rooms: Z", mazeGenerator.getRoomNumbers()[2], 1, 20);
                element.addPropertyListener(this);
                return element;
            }
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("xShift".equals(element.getID()))
        {
            BlockCoord shift = mazeGenerator.getStructureShift();
            mazeGenerator.setStructureShift(new BlockCoord((int) element.getPropertyValue(), shift.y, shift.z));
        }
        else if ("yShift".equals(element.getID()))
        {
            BlockCoord shift = mazeGenerator.getStructureShift();
            mazeGenerator.setStructureShift(new BlockCoord(shift.x, (int) element.getPropertyValue(), shift.z));
        }
        else if ("zShift".equals(element.getID()))
        {
            BlockCoord shift = mazeGenerator.getStructureShift();
            mazeGenerator.setStructureShift(new BlockCoord(shift.x, shift.y, (int) element.getPropertyValue()));
        }
        else if ("roomSizeX".equals(element.getID()))
        {
            int[] size = mazeGenerator.getRoomSize();
            size[0] = (int) element.getPropertyValue();
            mazeGenerator.setRoomSize(size);
        }
        else if ("roomSizeY".equals(element.getID()))
        {
            int[] size = mazeGenerator.getRoomSize();
            size[1] = (int) element.getPropertyValue();
            mazeGenerator.setRoomSize(size);
        }
        else if ("roomSizeZ".equals(element.getID()))
        {
            int[] size = mazeGenerator.getRoomSize();
            size[2] = (int) element.getPropertyValue();
            mazeGenerator.setRoomSize(size);
        }
        else if ("roomsX".equals(element.getID()))
        {
            int[] size = mazeGenerator.getRoomNumbers();
            size[0] = (int) element.getPropertyValue();
            mazeGenerator.setRoomNumbers(size);
        }
        else if ("roomsY".equals(element.getID()))
        {
            int[] size = mazeGenerator.getRoomNumbers();
            size[1] = (int) element.getPropertyValue();
            mazeGenerator.setRoomNumbers(size);
        }
        else if ("roomsZ".equals(element.getID()))
        {
            int[] size = mazeGenerator.getRoomNumbers();
            size[2] = (int) element.getPropertyValue();
            mazeGenerator.setRoomNumbers(size);
        }
    }

    @Override
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {
        if ("components".equals(tableElementButton.getID()))
        {
            tableNavigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazeComponentList(mazeGenerator.mazeComponents, tableDelegate, tableNavigator)));
        }
        else if ("exits".equals(tableElementButton.getID()))
        {
            tableNavigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazeExitList(mazeGenerator.mazeExits, tableDelegate, tableNavigator)));
        }
    }
}
