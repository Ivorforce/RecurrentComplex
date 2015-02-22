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
public class TableDataSourceMazePathList extends TableDataSourceList<MazePath, List<MazePath>>
{
    private int[] dimensions;

    public TableDataSourceMazePathList(List<MazePath> list, int[] dimensions, TableDelegate tableDelegate, TableNavigator navigator)
    {
        super(list, tableDelegate, navigator);
        this.dimensions = dimensions;
        setEarlierTitle("Up");
        setLaterTitle("Down");
    }

    @Override
    public String getDisplayString(MazePath mazePath)
    {
        return String.format("%s (%s)", Arrays.toString(mazePath.getSourceRoom().coordinates), TableDataSourceMazePath.directionFromPath(mazePath).toString());
    }

    @Override
    public MazePath newEntry(String actionID)
    {
        return new MazePath(2, false, new int[dimensions.length]);
    }

    @Override
    public TableDataSource editEntryDataSource(MazePath entry)
    {
        return new TableDataSourceMazePath(entry, dimensions);
    }
}
