/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.maze.SavedMazePathConnection;
import ivorius.reccomplex.utils.IvTranslations;

/**
 * Created by lukas on 22.06.14.
 */
public class TableDataSourceMazePathConnection extends TableDataSourceSegmented
{
    public TableDataSourceMazePathConnection(SavedMazePathConnection mazePath, int[] boundsLower, int[] boundsHigher, TableDelegate tableDelegate)
    {
        addManagedSection(0, new TableDataSourceConnector(mazePath.connector, IvTranslations.get("reccomplex.maze.connector")));
        addManagedSection(1, new TableDataSourceMazePath(mazePath.path, boundsLower, boundsHigher, tableDelegate));
    }
}
