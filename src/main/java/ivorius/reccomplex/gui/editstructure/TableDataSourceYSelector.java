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
public class TableDataSourceYSelector implements TableDataSource
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
            TableCellEnum<GenericYSelector.SelectionMode> cell = new TableCellEnum<>("ySelType", ySelector.selectionMode, TableCellEnum.options(Arrays.asList(GenericYSelector.SelectionMode.values()), "structures.genY.", true));
            cell.addPropertyConsumer(val -> ySelector.selectionMode = val);
            return new TableElementCell(IvTranslations.get("reccomplex.yselector.base"), cell);
        }
        else if (index == 1)
        {
            TableCellIntegerRange cell = new TableCellIntegerRange("ySelShift", new IntegerRange(ySelector.minYShift, ySelector.maxYShift), -100, 100);
            cell.addPropertyConsumer(val -> {
                ySelector.minYShift = val.getMin();
                ySelector.maxYShift = val.getMax();
            });
            return new TableElementCell(IvTranslations.get("reccomplex.yselector.shift"), cell).withTitleTooltip(IvTranslations.formatLines("reccomplex.structure.randomrange"));
        }

        return null;
    }
}
