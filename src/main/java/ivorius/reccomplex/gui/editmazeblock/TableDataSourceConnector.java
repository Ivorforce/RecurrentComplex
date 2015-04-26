/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editmazeblock;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.maze.SavedConnector;
import ivorius.reccomplex.utils.IvTranslations;

/**
 * Created by lukas on 26.04.15.
 */
public class TableDataSourceConnector implements TableDataSource, TableCellPropertyListener
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
        cell.addPropertyListener(this);
        cell.setTooltip(IvTranslations.formatLines("reccomplex.maze.connector.tooltip"));
        return new TableElementCell(title, cell);
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        connector.id = (String) cell.getPropertyValue();
    }
}
