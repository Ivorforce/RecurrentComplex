/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editmazeblock;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.Selection;

/**
* Created by lukas on 08.10.14.
*/
public class TableDataSourceSelectionArea extends TableDataSourceSegmented implements TableElementButton.Listener, TableElementPropertyListener
{
    private Selection.Area area;

    private int[] dimensions;

    public TableDataSourceSelectionArea(Selection.Area area, int[] dimensions)
    {
        this.area = area;
        this.dimensions = dimensions;
    }

    @Override
    public int numberOfSegments()
    {
        return 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 1 ? 3 : 1;
    }

    @Override
    public TableElement elementForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 0)
        {
            TableElementBoolean element = new TableElementBoolean("additive", "Additive", area.isAdditive());
            element.addPropertyListener(this);
            return element;
        }
        else if (segment == 1)
        {
            String title = String.format("Range: %s", index == 0 ? "X" : index == 1 ? "Y" : index == 2 ? "Z" : "" + index);
            IntegerRange intRange = new IntegerRange(area.getMinCoord()[index], area.getMaxCoord()[index]);
            TableElementIntegerRange element = new TableElementIntegerRange("area" + index, title, intRange, 0, dimensions[index] - 1);
            element.addPropertyListener(this);
            return element;
        }

        return null;
    }

    @Override
    public void actionPerformed(TableElementButton tableElementButton, String actionID)
    {

    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        if (element.getID().startsWith("area"))
        {
            int dim = Integer.valueOf(element.getID().substring(4));
            IntegerRange range = (IntegerRange) element.getPropertyValue();
            area.setCoord(dim, range.getMin(), range.getMax());
        }
        else if ("additive".equals(element.getID()))
        {
            area.setAdditive((Boolean) element.getPropertyValue());
        }
    }
}
