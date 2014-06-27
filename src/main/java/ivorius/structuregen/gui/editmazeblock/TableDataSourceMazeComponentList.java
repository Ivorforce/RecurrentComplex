/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.structuregen.gui.editmazeblock;

import ivorius.structuregen.gui.table.*;
import ivorius.ivtoolkit.maze.MazeComponent;
import ivorius.ivtoolkit.maze.MazePath;
import ivorius.ivtoolkit.maze.MazeRoom;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceMazeComponentList implements TableDataSource, TableElementButton.Listener
{
    private List<MazeComponent> mazeComponentList;

    private TableDelegate tableDelegate;
    private TableNavigator navigator;

    public TableDataSourceMazeComponentList(List<MazeComponent> mazeComponentList, TableDelegate tableDelegate, TableNavigator navigator)
    {
        this.mazeComponentList = mazeComponentList;
        this.tableDelegate = tableDelegate;
        this.navigator = navigator;
    }

    public List<MazeComponent> getMazeComponentList()
    {
        return Collections.unmodifiableList(mazeComponentList);
    }

    public void setMazeComponentList(List<MazeComponent> mazeComponentList)
    {
        this.mazeComponentList = mazeComponentList;
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
        return index >= 0 && index < mazeComponentList.size() + 1;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        if (index == mazeComponentList.size())
        {
            TableElementButton addButton = new TableElementButton("addComponent", "Add", new TableElementButton.Action("addComponent", "Add Component"));
            addButton.addListener(this);
            return addButton;
        }

        TableElementButton button = new TableElementButton("mazeComponent" + index, mazeComponentList.get(index).getIdentifier(), new TableElementButton.Action("edit", "Edit"), new TableElementButton.Action("delete", "Delete"));
        button.addListener(this);
        return button;
    }

    @Override
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {
        if (actionID.equals("addComponent"))
        {
            MazeComponent component = new MazeComponent(100, "", Arrays.asList(new MazeRoom(0, 0, 0)), Arrays.asList(new MazePath(0, false, 0, 0, 0), new MazePath(0, true, 0, 0, 0), new MazePath(2, false, 0, 0, 0), new MazePath(2, true, 0, 0, 0)));
            mazeComponentList.add(component);
            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazeComponent(component, tableDelegate, navigator)));
        }
        else if (tableElementButton.getID().startsWith("mazeComponent"))
        {
            int index = Integer.valueOf(tableElementButton.getID().substring(13));
            MazeComponent component = mazeComponentList.get(index);

            if (actionID.equals("edit"))
            {
                navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazeComponent(component, tableDelegate, navigator)));
            }
            else if (actionID.equals("delete"))
            {
                mazeComponentList.remove(component);
                tableDelegate.reloadData();
            }
        }
    }
}
