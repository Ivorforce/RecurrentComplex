/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editmazeblock;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.worldgen.genericStructures.MazeRoomArea;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceMazeAreaList extends TableDataSourceSegmented implements TableElementButton.Listener
{
    private List<MazeRoomArea> mazeRoomList;
    private int[] dimensions;

    private TableDelegate tableDelegate;
    private TableNavigator navigator;

    public TableDataSourceMazeAreaList(List<MazeRoomArea> mazeRoomList, int[] dimensions, TableDelegate tableDelegate, TableNavigator navigator)
    {
        this.mazeRoomList = mazeRoomList;
        this.dimensions = dimensions;
        this.tableDelegate = tableDelegate;
        this.navigator = navigator;
    }

    public List<MazeRoomArea> getMazeRoomList()
    {
        return mazeRoomList;
    }

    public void setMazeRoomList(List<MazeRoomArea> mazeRoomList)
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
    public int numberOfSegments()
    {
        return 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 0 ? mazeRoomList.size() : 1;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            String title = String.format("%s - %s", Arrays.toString(mazeRoomList.get(index).getMinCoord()), Arrays.toString(mazeRoomList.get(index).getMaxCoord()));
            TableElementButton button = new TableElementButton("area" + index, title, new TableElementButton.Action("edit", "Edit"), new TableElementButton.Action("delete", "Delete"));
            button.addListener(this);
            return button;
        }
        else if (segment == 1)
        {
            if (index == 0)
            {
                TableElementButton addButton = new TableElementButton("addArea", "Add", new TableElementButton.Action("addArea", "Add Area"));
                addButton.addListener(this);
                return addButton;
            }
        }

        return null;
    }

    @Override
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {
        if (actionID.equals("addArea"))
        {
            MazeRoomArea area = new MazeRoomArea(new int[dimensions.length], new int[dimensions.length]);
            mazeRoomList.add(area);
            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazeArea(area, dimensions)));
        }
        else if (tableElementButton.getID().startsWith("area"))
        {
            int index = Integer.valueOf(tableElementButton.getID().substring(4));
            MazeRoomArea room = mazeRoomList.get(index);

            if (actionID.equals("edit"))
            {
                navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazeArea(room, dimensions)));
            }
            else if (actionID.equals("delete"))
            {
                mazeRoomList.remove(room);
                tableDelegate.reloadData();
            }
        }
    }

}
