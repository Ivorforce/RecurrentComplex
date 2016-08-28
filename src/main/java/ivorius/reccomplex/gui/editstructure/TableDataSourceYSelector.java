/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.GenericYSelector;
import ivorius.ivtoolkit.tools.IvTranslations;

import java.util.Arrays;

/**
 * Created by lukas on 04.04.15.
 */
public class TableDataSourceYSelector implements TableDataSource, TableCellPropertyListener
{
    public GenericYSelector ySelector;

    public TableDataSourceYSelector(GenericYSelector ySelector)
    {
        this.ySelector = ySelector;
    }

    @Override
    public int numberOfElements()
    {
        return 2;
    }

    @Override
    public TableElement elementForIndex(GuiTable table, int index)
    {
        if (index == 0)
        {
            TableCellEnum cell = new TableCellEnum<>("ySelType", ySelector.selectionMode, TableCellEnum.options(Arrays.asList(GenericYSelector.SelectionMode.values()), "structures.genY.", true));
            cell.addPropertyListener(this);
            return new TableElementCell(IvTranslations.get("reccomplex.yselector.base"), cell);
        }
        else if (index == 1)
        {
            TableCellIntegerRange cell = new TableCellIntegerRange("ySelShift", new IntegerRange(ySelector.minYShift, ySelector.maxYShift), -100, 100);
            cell.setTooltip(IvTranslations.formatLines("reccomplex.structure.randomrange"));
            cell.addPropertyListener(this);
            return new TableElementCell(IvTranslations.get("reccomplex.yselector.shift"), cell);
        }

        return null;
    }

    @Override
    public void valueChanged(TableCellPropertyDefault cell)
    {
        if (cell.getID() != null)
        {
            switch (cell.getID())
            {
                case "ySelType":
                    ySelector.selectionMode = (GenericYSelector.SelectionMode) cell.getPropertyValue();
                    break;
                case "ySelShift":
                    IntegerRange range = ((IntegerRange) cell.getPropertyValue());
                    ySelector.minYShift = range.getMin();
                    ySelector.maxYShift = range.getMax();
                    break;
            }
        }
    }
}
