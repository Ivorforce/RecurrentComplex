/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.worldscripts.mazegenerator;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.cell.TableCell;
import ivorius.reccomplex.gui.table.cell.TableCellString;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSource;
import ivorius.reccomplex.world.gen.feature.structure.generic.maze.SavedConnector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 26.04.15.
 */

@SideOnly(Side.CLIENT)
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
    public int numberOfCells()
    {
        return 1;
    }

    @Override
    public TableCell cellForIndex(GuiTable table, int index)
    {
        TableCellString cell = new TableCellString("connectorID", connector.id);
        cell.addListener(val -> connector.id = val);
        return new TitledCell(title, cell).withTitleTooltip(IvTranslations.formatLines("reccomplex.maze.connector.tooltip"));
    }
}
