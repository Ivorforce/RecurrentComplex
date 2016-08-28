/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.inventorygen;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;

/**
 * Created by lukas on 17.01.15.
 */
public class TableDataSourceInvGenMultiTag extends TableDataSourceSegmented implements TableCellPropertyListener
{
    public IntegerRange itemCount;

    public TableDataSourceInvGenMultiTag(IntegerRange itemCount)
    {
        this.itemCount = itemCount;
    }

    @Override
    public int numberOfSegments()
    {
        return 1;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return 1;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            if (index == 0)
            {
                TableCellIntegerRange cell = new TableCellIntegerRange("itemCount", itemCount, 0, 64);
                cell.addPropertyListener(this);
                return new TableElementCell(IvTranslations.get("reccomplex.gui.inventorygen.multi.count"), cell);
            }
        }

        return null;
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if ("itemCount".equals(cell.getID()))
            itemCount = (IntegerRange) cell.getPropertyValue();
    }
}
