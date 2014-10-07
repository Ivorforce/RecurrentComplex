/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.maze.MazePath;
import ivorius.ivtoolkit.maze.MazeRoom;
import ivorius.ivtoolkit.tools.IvCollections;
import ivorius.reccomplex.gui.editmazeblock.TableDataSourceMazePath;
import ivorius.reccomplex.gui.editmazeblock.TableDataSourceMazeRoom;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.worldgen.genericStructures.GenericStructureInfo;
import ivorius.reccomplex.worldgen.genericStructures.MazeComponent;
import ivorius.reccomplex.worldgen.genericStructures.gentypes.MazeGenerationInfo;

import java.util.Arrays;

/**
 * Created by lukas on 07.10.14.
 */
public class TableDataSourceMazeGenerationInfo extends TableDataSourceSegmented implements TableElementButton.Listener, TableElementPropertyListener
{
    private TableNavigator navigator;
    private TableDelegate tableDelegate;

    private GenericStructureInfo structureInfo;

    public TableDataSourceMazeGenerationInfo(TableNavigator navigator, TableDelegate tableDelegate, GenericStructureInfo structureInfo)
    {
        this.navigator = navigator;
        this.tableDelegate = tableDelegate;
        this.structureInfo = structureInfo;
    }

    @Override
    public int numberOfSegments()
    {
        return 7;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        switch (segment)
        {
            case 0:
                return 2;
            case 1:
                return 1;
            case 2:
                return mazeComponent().getRooms().size();
            case 3:
                return 1;
            case 4:
                return 1;
            case 5:
                return mazeComponent().getExitPaths().size();
            case 6:
                return 1;
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
                TableElementString element = new TableElementString("mazeID", "Maze ID", structureInfo.mazeGenerationInfo.mazeID);
                element.addPropertyListener(this);
                return element;
            }
            else if (index == 1)
            {
                TableElementInteger element = new TableElementInteger("weight", "Spawn Weight", mazeComponent().itemWeight, 0, 500);
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
            MazeRoom room = mazeComponent().getRooms().get(index);
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
            MazePath exit = mazeComponent().getExitPaths().get(index);
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
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {
        if ("addRoom".equals(tableElementButton.getID()))
        {
            MazeRoom newRoom = new MazeRoom(0, 0, 0);
            mazeComponent().setRooms(IvCollections.modifiableCopyWith(mazeComponent().getRooms(), newRoom));
            tableDelegate.reloadData();

            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazeRoom(newRoom)));
        }
        else if ("addExit".equals(tableElementButton.getID()))
        {
            MazePath newExit = new MazePath(0, false, 0, 0, 0);
            mazeComponent().setExitPaths(IvCollections.modifiableCopyWith(mazeComponent().getExitPaths(), newExit));
            tableDelegate.reloadData();

            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazePath(newExit)));
        }
        else if (tableElementButton.getID().startsWith("room"))
        {
            int index = Integer.valueOf(tableElementButton.getID().substring(4));

            if (actionID.equals("edit"))
            {
                navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazeRoom(mazeComponent().getRooms().get(index))));
            }
            else if (actionID.equals("delete"))
            {
                mazeComponent().setRooms(IvCollections.modifiableCopyWithout(mazeComponent().getRooms(), index));
                tableDelegate.reloadData();
            }
        }
        else if (tableElementButton.getID().startsWith("exit"))
        {
            int index = Integer.valueOf(tableElementButton.getID().substring(4));

            if (actionID.equals("edit"))
            {
                navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazePath(mazeComponent().getExitPaths().get(index))));
            }
            else if (actionID.equals("delete"))
            {
                mazeComponent().setExitPaths(IvCollections.modifiableCopyWithout(mazeComponent().getExitPaths(), index));
                tableDelegate.reloadData();
            }
        }
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if ("mazeID".equals(element.getID()))
        {
            structureInfo.mazeGenerationInfo.mazeID = (String) element.getPropertyValue();
        }
        else if ("weight".equals(element.getID()))
        {
            mazeComponent().itemWeight = ((int) element.getPropertyValue());
        }
    }

    private MazeComponent mazeComponent()
    {
        return structureInfo.mazeGenerationInfo.mazeComponent;
    }
}
