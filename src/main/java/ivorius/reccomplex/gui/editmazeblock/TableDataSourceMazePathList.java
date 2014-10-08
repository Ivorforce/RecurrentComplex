/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editmazeblock;

import ivorius.ivtoolkit.maze.MazePath;
import ivorius.reccomplex.gui.table.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceMazePathList implements TableDataSource, TableElementButton.Listener
{
    private List<MazePath> mazePathList;
    private int[] dimensions;

    private TableDelegate tableDelegate;
    private TableNavigator navigator;

    public TableDataSourceMazePathList(List<MazePath> mazePathList, int[] dimensions, TableDelegate tableDelegate, TableNavigator navigator)
    {
        this.mazePathList = mazePathList;
        this.dimensions = dimensions;
        this.tableDelegate = tableDelegate;
        this.navigator = navigator;
    }

    public List<MazePath> getMazePathList()
    {
        return mazePathList;
    }

    public void setMazePathList(List<MazePath> mazePathList)
    {
        this.mazePathList = mazePathList;
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
        return index >= 0 && index < mazePathList.size() + 1;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        if (index == mazePathList.size())
        {
            TableElementButton addButton = new TableElementButton("addPath", "Add", new TableElementButton.Action("addPath", "Add Path"));
            addButton.addListener(this);
            return addButton;
        }

        MazePath mazePath = mazePathList.get(index);
        String title = String.format("%s (%s)", Arrays.toString(mazePath.getSourceRoom().coordinates), TableDataSourceMazePath.directionFromPath(mazePath).toString());
        TableElementButton button = new TableElementButton("mazePath" + index, title, new TableElementButton.Action("edit", "Edit"), new TableElementButton.Action("delete", "Delete"));
        button.addListener(this);
        return button;
    }

    @Override
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {
        if (actionID.equals("addPath"))
        {
            MazePath path = new MazePath(2, false, new int[dimensions.length]);
            mazePathList.add(path);

            navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazePath(path, dimensions)));
        }
        else if (tableElementButton.getID().startsWith("mazePath"))
        {
            int index = Integer.valueOf(tableElementButton.getID().substring(8));
            MazePath path = mazePathList.get(index);

            if (actionID.equals("edit"))
            {
                navigator.pushTable(new GuiTable(tableDelegate, new TableDataSourceMazePath(path, dimensions)));
            }
            else if (actionID.equals("delete"))
            {
                mazePathList.remove(path);
                tableDelegate.reloadData();
            }
        }
    }
}
