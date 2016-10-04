/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.table.datasource;

import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.TableElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourceSupplied implements TableDataSource
{
    public final List<Supplier<TableElement>> elements = new ArrayList<>();

    @SafeVarargs
    public TableDataSourceSupplied(Supplier<TableElement>... elements)
    {
        Collections.addAll(this.elements, elements);
    }

    public TableDataSourceSupplied(List<Supplier<TableElement>> elements)
    {
        this.elements.addAll(elements);
    }

    public List<Supplier<TableElement>> getElements()
    {
        return elements;
    }

    public void setElements(List<Supplier<TableElement>> elements)
    {
        this.elements.clear();
        this.elements.addAll(elements);
    }

    @Override
    public int numberOfElements()
    {
        return elements.size();
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        return elements.get(index).get();
    }
}
