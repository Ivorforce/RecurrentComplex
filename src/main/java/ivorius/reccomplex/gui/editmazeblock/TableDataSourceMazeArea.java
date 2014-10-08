/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editmazeblock;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.worldgen.genericStructures.MazeRoomArea;

/**
* Created by lukas on 08.10.14.
*/
public class TableDataSourceMazeArea implements TableDataSource, TableElementButton.Listener, TableElementPropertyListener
{
    private MazeRoomArea area;

    private int[] dimensions;

    public TableDataSourceMazeArea(MazeRoomArea area, int[] dimensions)
    {
        this.area = area;
        this.dimensions = dimensions;
    }

    @Override
    public boolean has(GuiTable table, int index)
    {
        return index >= 0 && index <= 2;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        if (index < 3)
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
    }
}
