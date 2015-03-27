/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.inventorygen;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.table.*;

import java.util.Objects;

/**
 * Created by lukas on 17.01.15.
 */
public class TableDataSourceInvGenMultiTag extends TableDataSourceSegmented implements TableElementPropertyListener
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
                TableElementIntegerRange element = new TableElementIntegerRange("itemCount", "Item Count", itemCount, 0, 64);
                element.addPropertyListener(this);
                return element;
            }
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if (Objects.equals(element.getID(), "itemCount"))
            itemCount = (IntegerRange) element.getPropertyValue();
    }
}
