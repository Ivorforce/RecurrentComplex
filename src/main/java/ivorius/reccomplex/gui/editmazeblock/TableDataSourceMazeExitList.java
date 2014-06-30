/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editmazeblock;

import ivorius.reccomplex.gui.table.*;
import ivorius.ivtoolkit.maze.MazePath;

import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceMazeExitList implements TableDataSource, TableElementButton.Listener
{
    private List<MazePath> mazeExitList;

    private TableDelegate tableDelegate;
    private TableNavigator navigator;

    public TableDataSourceMazeExitList(List<MazePath> mazeExitList, TableDelegate tableDelegate, TableNavigator navigator)
    {
        this.mazeExitList = mazeExitList;
        this.tableDelegate = tableDelegate;
        this.navigator = navigator;
    }

    public List<MazePath> getMazeExitList()
    {
        return mazeExitList;
    }

    public void setMazeExitList(List<MazePath> mazeExitList)
    {
        this.mazeExitList = mazeExitList;
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
        return index >= 0 && index < mazeExitList.size() + 1;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        if (index == mazeExitList.size())
        {
            TableElementButton addButton = new TableElementButton("addExit", "Add", new TableElementButton.Action("addExit", "Add Exit"));
            addButton.addListener(this);
            return addButton;
        }

        TableElementButton button = new TableElementButton("mazeExit" + index, "Exit", new TableElementButton.Action("edit", "Edit"), new TableElementButton.Action("delete", "Delete"));
        button.addListener(this);
        return button;
    }

    @Override
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {
        if (actionID.equals("addExit"))
        {
            MazePath exit = new MazePath(2, false, 0, 0, 0);
            mazeExitList.add(exit);

            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazePath(exit)));
        }
        else if (tableElementButton.getID().startsWith("mazeExit"))
        {
            int index = Integer.valueOf(tableElementButton.getID().substring(8));
            MazePath exit = mazeExitList.get(index);

            if (actionID.equals("edit"))
            {
                navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazePath(exit)));
            }
            else if (actionID.equals("delete"))
            {
                mazeExitList.remove(exit);
                tableDelegate.reloadData();
            }
        }
    }
}
