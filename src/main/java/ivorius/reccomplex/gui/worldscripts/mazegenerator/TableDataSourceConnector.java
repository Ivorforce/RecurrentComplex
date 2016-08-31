/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.maze.SavedConnector;
import ivorius.ivtoolkit.tools.IvTranslations;

/**
 * Created by lukas on 26.04.15.
 */
public class TableDataSourceConnector implements TableDataSource
{
    public SavedConnector connector;
    public String title;

    public TableDataSourceConnector(SavedConnector connector, String title)
    {
        this.connector = connector;
        this.title = title;
    }

    @Override
    public int numberOfElements()
    {
        return 1;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        TableCellString cell = new TableCellString("connectorID", connector.id);
        cell.addPropertyConsumer(val -> connector.id = val);
        cell.setTooltip(IvTranslations.formatLines("reccomplex.maze.connector.tooltip"));
        return new TableElementCell(title, cell);
    }
}
