/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editmazeblock;

import ivorius.ivtoolkit.maze.MazePath;
import ivorius.reccomplex.gui.table.*;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceMazePathList extends TableDataSourceList<MazePath, List<MazePath>>
{
    private int[] boundsLower;
    private int[] boundsHigher;

    public TableDataSourceMazePathList(List<MazePath> list, TableDelegate tableDelegate, TableNavigator navigator, int[] boundsLower, int[] boundsHigher)
    {
        super(list, tableDelegate, navigator);
        this.boundsLower = boundsLower;
        this.boundsHigher = boundsHigher;
        setEarlierTitle("Up");
        setLaterTitle("Down");
    }

    @Override
    public String getDisplayString(MazePath mazePath)
    {
        return String.format("%s %s%s", Arrays.toString(mazePath.getSourceRoom().coordinates), EnumChatFormatting.BLUE, TableDataSourceMazePath.directionFromPath(mazePath).toString());
    }

    @Override
    public MazePath newEntry(String actionID)
    {
        return new MazePath(2, false, new int[boundsLower.length]);
    }

    @Override
    public TableDataSource editEntryDataSource(MazePath entry)
    {
        return new TableDataSourceMazePath(entry, boundsLower, boundsHigher);
    }
}
