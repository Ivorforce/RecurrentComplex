/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.datasource;

import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.cell.TableCell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceSupplied implements TableDataSource
{
    public final List<Supplier<TableCell>> cells = new ArrayList<>();

    @SafeVarargs
    public TableDataSourceSupplied(Supplier<TableCell>... cells)
    {
        Collections.addAll(this.cells, cells);
    }

    public TableDataSourceSupplied(List<Supplier<TableCell>> cells)
    {
        this.cells.addAll(cells);
    }

    public List<Supplier<TableCell>> getCells()
    {
        return cells;
    }

    public void setCells(List<Supplier<TableCell>> cells)
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
        return cells.get(index).get();
    }
}
