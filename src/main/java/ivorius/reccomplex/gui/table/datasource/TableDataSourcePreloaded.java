/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.datasource;

import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.cell.TableCell;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourcePreloaded implements TableDataSource
{
    public final List<TableCell> cells = new ArrayList<>();

    public TableDataSourcePreloaded(TableCell... cells)
    {
        Collections.addAll(this.cells, cells);
    }

    public TableDataSourcePreloaded(List<TableCell> cells)
    {
        this.cells.addAll(cells);
    }

    public List<TableCell> getCells()
    {
        return cells;
    }

    public void setCells(List<TableCell> cells)
    {
        this.cells.clear();
        this.cells.addAll(cells);
    }

    @Override
    public int numberOfCells()
    {
        return cells.size();
    }

    @Override
    public TableCell cellForIndex(GuiTable table, int index)
    {
        return cells.get(index);
    }
}
