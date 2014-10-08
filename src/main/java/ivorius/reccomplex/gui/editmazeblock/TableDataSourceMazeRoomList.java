/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editmazeblock;

import ivorius.ivtoolkit.maze.MazePath;
import ivorius.ivtoolkit.maze.MazeRoom;
import ivorius.reccomplex.gui.table.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceMazeRoomList implements TableDataSource, TableElementButton.Listener
{
    private List<MazeRoom> mazeRoomList;
    private int[] dimensions;

    private TableDelegate tableDelegate;
    private TableNavigator navigator;

    public TableDataSourceMazeRoomList(List<MazeRoom> mazeRoomList, int[] dimensions, TableDelegate tableDelegate, TableNavigator navigator)
    {
        this.mazeRoomList = mazeRoomList;
        this.dimensions = dimensions;
        this.tableDelegate = tableDelegate;
        this.navigator = navigator;
    }

    public List<MazeRoom> getMazeRoomList()
    {
        return mazeRoomList;
    }

    public void setMazeRoomList(List<MazeRoom> mazeRoomList)
    {
        this.mazeRoomList = mazeRoomList;
    }

    public TableDelegate getTableDelegate()
    {
        return tableDelegate;
    }

    public void setTableDelegate(TableDelegate tableDelegate)
    {
        this.tableDelegate = tableDelegate;
    }

    public TableNavigator getNavigator()
    {
        return navigator;
    }

    public void setNavigator(TableNavigator navigator)
    {
        this.navigator = navigator;
    }

    @Override
    public boolean has(GuiTable table, int index)
    {
        return index >= 0 && index < mazeRoomList.size() + 1;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        if (index == mazeRoomList.size())
        {
            TableElementButton addButton = new TableElementButton("addRoom", "Add", new TableElementButton.Action("addRoom", "Add Room"));
            addButton.addListener(this);
            return addButton;
        }

        String title = "Room " + Arrays.toString(mazeRoomList.get(index).coordinates);
        TableElementButton button = new TableElementButton("mazeRoom" + index, title, new TableElementButton.Action("edit", "Edit"), new TableElementButton.Action("delete", "Delete"));
        button.addListener(this);
        return button;
    }

    @Override
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {
        if (actionID.equals("addRoom"))
        {
            MazeRoom room = new MazeRoom(new int[dimensions.length]);
            mazeRoomList.add(room);

            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazeRoom(room, dimensions)));
        }
        else if (tableElementButton.getID().startsWith("mazeRoom"))
        {
            int index = Integer.valueOf(tableElementButton.getID().substring(8));
            MazeRoom room = mazeRoomList.get(index);

            if (actionID.equals("edit"))
            {
                navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazeRoom(room, dimensions)));
            }
            else if (actionID.equals("delete"))
            {
                mazeRoomList.remove(room);
                tableDelegate.reloadData();
            }
        }
    }
}
