/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator.rules;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.TableDataSourceMazePathList;
import ivorius.reccomplex.structures.generic.maze.rules.saved.SavedReachabilityStrategy;
import ivorius.reccomplex.utils.IvTranslations;

/**
 * Created by lukas on 21.03.16.
 */
public class TableDataSourceReachabilityStrategy extends TableDataSourceSegmented
{
    public TableDataSourceReachabilityStrategy(SavedReachabilityStrategy strategy, TableDelegate tableDelegate, TableNavigator navigator, int[] boundsLower, int[] boundsHigher)
    {
        TableCellTitle startTitle = new TableCellTitle("", "Start");
        startTitle.setTooltip(IvTranslations.formatLines("reccomplex.mazerule.connect.start.tooltip"));
        addManagedSection(0, new TableDataSourcePreloaded(new TableElementCell(startTitle)));
        addManagedSection(1, new TableDataSourceMazePathList(strategy.start, tableDelegate, navigator, boundsLower, boundsHigher));

        TableCellTitle endTitle = new TableCellTitle("", "End");
        endTitle.setTooltip(IvTranslations.formatLines("reccomplex.mazerule.connect.end.tooltip"));
        addManagedSection(2, new TableDataSourcePreloaded(new TableElementCell(endTitle)));
        addManagedSection(3, new TableDataSourceMazePathList(strategy.end, tableDelegate, navigator, boundsLower, boundsHigher));
    }
}
