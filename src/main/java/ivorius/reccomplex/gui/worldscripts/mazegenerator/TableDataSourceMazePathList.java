/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.maze.components.MazeRoom;
import ivorius.reccomplex.gui.table.TableDataSource;
import ivorius.reccomplex.gui.table.TableDataSourceList;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.structures.generic.maze.SavedMazePath;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceMazePathList extends TableDataSourceList<SavedMazePath, List<SavedMazePath>>
{
    private int[] boundsLower;
    private int[] boundsHigher;

    public TableDataSourceMazePathList(List<SavedMazePath> list, TableDelegate tableDelegate, TableNavigator navigator, int[] boundsLower, int[] boundsHigher)
    {
        super(list, tableDelegate, navigator);
        this.boundsLower = boundsLower;
        this.boundsHigher = boundsHigher;
        setEarlierTitle("Up");
        setLaterTitle("Down");
    }

    @Override
    public String getDisplayString(SavedMazePath mazePath)
    {
        return String.format("%s %s%s%s", Arrays.toString(mazePath.sourceRoom.getCoordinates()),
                EnumChatFormatting.BLUE, TableDataSourceMazePath.directionFromPath(mazePath).toString(), EnumChatFormatting.RESET);
    }

    @Override
    public SavedMazePath newEntry(String actionID)
    {
        return new SavedMazePath(2, new MazeRoom(new int[boundsLower.length]), false);
    }

    @Override
    public TableDataSource editEntryDataSource(SavedMazePath entry)
    {
        return new TableDataSourceMazePath(entry, boundsLower, boundsHigher, tableDelegate);
    }
}
