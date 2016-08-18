/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.maze.ConnectorStrategy;
import ivorius.reccomplex.structures.generic.maze.SavedMazePathConnection;
import com.mojang.realmsclient.gui.ChatFormatting;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceMazePathConnectionList extends TableDataSourceList<SavedMazePathConnection, List<SavedMazePathConnection>>
{
    private int[] boundsLower;
    private int[] boundsHigher;

    public TableDataSourceMazePathConnectionList(List<SavedMazePathConnection> list, TableDelegate tableDelegate, TableNavigator navigator, int[] boundsLower, int[] boundsHigher)
    {
        super(list, tableDelegate, navigator);
        this.boundsLower = boundsLower;
        this.boundsHigher = boundsHigher;
        setEarlierTitle("Up");
        setLaterTitle("Down");
    }

    @Override
    public String getDisplayString(SavedMazePathConnection mazePath)
    {
        return String.format("%s %s%s%s", Arrays.toString(mazePath.path.sourceRoom.getCoordinates()),
                ChatFormatting.BLUE, TableDataSourceMazePath.directionFromPath(mazePath.path).toString(), ChatFormatting.RESET);
    }

    @Override
    public SavedMazePathConnection newEntry(String actionID)
    {
        return new SavedMazePathConnection(2, new MazeRoom(new int[boundsLower.length]), false, ConnectorStrategy.DEFAULT_PATH);
    }

    @Override
    public TableDataSource editEntryDataSource(SavedMazePathConnection entry)
    {
        return new TableDataSourceMazePathConnection(entry, boundsLower, boundsHigher, tableDelegate);
    }
}
