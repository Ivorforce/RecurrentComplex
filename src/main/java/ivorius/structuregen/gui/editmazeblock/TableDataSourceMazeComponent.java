/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.gui.editmazeblock;

import ivorius.structuregen.gui.GuiValidityStateIndicator;
import ivorius.structuregen.gui.table.*;
import ivorius.structuregen.ivtoolkit.IvCollections;
import ivorius.structuregen.ivtoolkit.maze.MazeComponent;
import ivorius.structuregen.ivtoolkit.maze.MazePath;
import ivorius.structuregen.ivtoolkit.maze.MazeRoom;
import ivorius.structuregen.worldgen.StructureHandler;

import java.util.Arrays;

/**
 * Created by lukas on 05.06.14.
 */
public class TableDataSourceMazeComponent extends TableDataSourceSegmented implements TableElementPropertyListener, TableElementButton.Listener
{
    private MazeComponent mazeComponent;

    private TableDelegate tableDelegate;
    private TableNavigator tableNavigator;

    public TableDataSourceMazeComponent(MazeComponent mazeComponent, TableDelegate tableDelegate, TableNavigator tableNavigator)
    {
        this.mazeComponent = mazeComponent;
        this.tableDelegate = tableDelegate;
        this.tableNavigator = tableNavigator;
    }

    public MazeComponent getMazeComponent()
    {
        return mazeComponent;
    }

    public void setMazeComponent(MazeComponent mazeComponent)
    {
        this.mazeComponent = mazeComponent;
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
        return 7;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        if (segment == 0)
        {
            return 2;
        }

        if (segment == 1 || segment == 3 || segment == 4 || segment == 6)
        {
            return 1;
        }

        if (segment == 2)
        {
            return mazeComponent.getRooms().size();
        }

        if (segment == 5)
        {
            return mazeComponent.getExitPaths().size();
        }

        return 0;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            if (index == 0)
            {
                TableElementString element = new TableElementString("structure", "Structure", mazeComponent.getIdentifier());
                element.addPropertyListener(this);
                element.setShowsValidityState(true);
                element.setValidityState(currentComponentIDState());
                return element;
            }
            else if (index == 1)
            {
                TableElementInteger element = new TableElementInteger("weight", "Weight", mazeComponent.itemWeight, 0, 500);
                element.addPropertyListener(this);
                return element;
            }
        }
        else if (segment == 1)
        {
            return new TableElementTitle("roomsTitle", "", "Rooms");
        }
        else if (segment == 2)
        {
            MazeRoom room = mazeComponent.getRooms().get(index);
            boolean canEdit = index > 0 || room.coordinates[0] != 0 || room.coordinates[1] != 0 || room.coordinates[2] != 0;
            String title = "Room " + Arrays.toString(room.coordinates);

            TableElementButton element = new TableElementButton("room" + index, title, new TableElementButton.Action("edit", "Edit", canEdit), new TableElementButton.Action("delete", "Delete", canEdit));
            element.addListener(this);
            return element;
        }
        else if (segment == 3)
        {
            TableElementButton element = new TableElementButton("addRoom", "Add Room", new TableElementButton.Action("add", "Add"));
            element.addListener(this);
            return element;
        }
        else if (segment == 4)
        {
            return new TableElementTitle("exitsTitle", "", "Exits");
        }
        else if (segment == 5)
        {
            MazePath exit = mazeComponent.getExitPaths().get(index);
            String title = "Exit " + Arrays.toString(exit.getDestinationRoom().coordinates);

            TableElementButton element = new TableElementButton("exit" + index, title, new TableElementButton.Action("edit", "Edit"), new TableElementButton.Action("delete", "Delete"));
            element.addListener(this);
            return element;
        }
        else if (segment == 6)
        {
            TableElementButton element = new TableElementButton("addExit", "Add Exit", new TableElementButton.Action("add", "Add"));
            element.addListener(this);
            return element;
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("structure".equals(element.getID()))
        {
            mazeComponent.setIdentifier(((String) element.getPropertyValue()));
            ((TableElementString) element).setValidityState(currentComponentIDState());
        }
        else if ("weight".equals(element.getID()))
        {
            mazeComponent.itemWeight = ((int) element.getPropertyValue());
        }
    }

    private GuiValidityStateIndicator.State currentComponentIDState()
    {
        return StructureHandler.getAllStructureNames().contains(mazeComponent.getIdentifier()) ? GuiValidityStateIndicator.State.VALID : GuiValidityStateIndicator.State.INVALID;
    }

    @Override
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {
        if ("addRoom".equals(tableElementButton.getID()))
        {
            MazeRoom newRoom = new MazeRoom(0, 0, 0);
            mazeComponent.setRooms(IvCollections.modifiableCopyWith(mazeComponent.getRooms(), newRoom));
            tableDelegate.reloadData();

            tableNavigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazeRoom(newRoom)));
        }
        else if ("addExit".equals(tableElementButton.getID()))
        {
            MazePath newExit = new MazePath(0, false, 0, 0, 0);
            mazeComponent.setExitPaths(IvCollections.modifiableCopyWith(mazeComponent.getExitPaths(), newExit));
            tableDelegate.reloadData();

            tableNavigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazePath(newExit)));
        }
        else if (tableElementButton.getID().startsWith("room"))
        {
            int index = Integer.valueOf(tableElementButton.getID().substring(4));

            if (actionID.equals("edit"))
            {
                tableNavigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazeRoom(mazeComponent.getRooms().get(index))));
            }
            else if (actionID.equals("delete"))
            {
                mazeComponent.setRooms(IvCollections.modifiableCopyWithout(mazeComponent.getRooms(), index));
                tableDelegate.reloadData();
            }
        }
        else if (tableElementButton.getID().startsWith("exit"))
        {
            int index = Integer.valueOf(tableElementButton.getID().substring(4));

            if (actionID.equals("edit"))
            {
                tableNavigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazePath(mazeComponent.getExitPaths().get(index))));
            }
            else if (actionID.equals("delete"))
            {
                mazeComponent.setExitPaths(IvCollections.modifiableCopyWithout(mazeComponent.getExitPaths(), index));
                tableDelegate.reloadData();
            }
        }
    }
}
