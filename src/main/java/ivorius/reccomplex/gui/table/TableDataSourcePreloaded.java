/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourcePreloaded implements TableDataSource
{
    public final List<TableElement> elements = new ArrayList<>();

    public TableDataSourcePreloaded(TableElement... elements)
    {
        Collections.addAll(this.elements, elements);
    }

    public TableDataSourcePreloaded(List<TableElement> elements)
    {
        this.elements.addAll(elements);
    }

    public List<TableElement> getElements()
    {
        return elements;
    }

    public void setElements(List<TableElement> elements)
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
        return elements.get(index);
    }
}
