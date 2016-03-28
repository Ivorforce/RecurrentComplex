/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator.reachability;

import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.TableDataSourceMazePath;
import ivorius.reccomplex.structures.generic.maze.SavedMazePath;
import ivorius.reccomplex.structures.generic.maze.SavedMazeReachability;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lukas on 16.03.16.
 */
public class TableDataSourcePathConnectionList extends TableDataSourceList<ImmutablePair<SavedMazePath, SavedMazePath>, List<ImmutablePair<SavedMazePath, SavedMazePath>>>
{
    private int[] boundsLower;
    private int[] boundsHigher;

    public TableDataSourcePathConnectionList(List<ImmutablePair<SavedMazePath, SavedMazePath>> list, TableDelegate tableDelegate, TableNavigator navigator, int[] boundsLower, int[] boundsHigher)
    {
        super(list, tableDelegate, navigator);
        this.boundsLower = boundsLower;
        this.boundsHigher = boundsHigher;
    }

    public TableDelegate getTableDelegate()
    {
        return tableDelegate;
    }

    public void setTableDelegate(TableDelegate tableDelegate)
    {
        this.tableDelegate = tableDelegate;
    }

    @Override
    public String getDisplayString(ImmutablePair<SavedMazePath, SavedMazePath> pair)
    {
        return pair.toString();
    }

    @Override
    public ImmutablePair<SavedMazePath, SavedMazePath> newEntry(String actionID)
    {
        return ImmutablePair.of(new SavedMazePath(0, new MazeRoom(0, 0, 0), true), new SavedMazePath(0, new MazeRoom(0, 0, 0), false));
    }

    @Override
    public TableDataSource editEntryDataSource(ImmutablePair<SavedMazePath, SavedMazePath> pair)
    {
        return new TableDataSourceMulti(
                new TableDataSourcePreloaded(new TableElementCell(new TableCellTitle("", "Source"))),
                new TableDataSourceMazePath(pair.getLeft(), boundsLower, boundsHigher,  tableDelegate),
                new TableDataSourcePreloaded(new TableElementCell(new TableCellTitle("", "Destination"))),
                new TableDataSourceMazePath(pair.getRight(), boundsLower, boundsHigher, tableDelegate)
        );
    }
}
