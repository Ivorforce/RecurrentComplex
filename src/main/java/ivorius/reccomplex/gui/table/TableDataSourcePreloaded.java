/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.table;

import java.util.List;

/**
 * Created by lukas on 04.06.14.
 */
public class TableDataSourcePreloaded implements TableDataSource
{
    private List<TableElement> elements;

    public TableDataSourcePreloaded(List<TableElement> elements)
    {
        this.elements = elements;
    }

    public List<TableElement> getElements()
    {
        return elements;
    }

    public void setElements(List<TableElement> elements)
    {
        this.elements = elements;
    }

    @Override
    public boolean has(GuiTable table, int index)
    {
        return index >= 0 && index < elements.size();
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        return elements.get(index);
    }
}
