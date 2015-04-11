/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.GenericYSelector;
import ivorius.reccomplex.utils.IvTranslations;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

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

    public static List<TableCellEnum.Option<GenericYSelector.SelectionMode>> allGenerationOptions()
    {
        List<TableCellEnum.Option<GenericYSelector.SelectionMode>> generationBases = new ArrayList<>();

        for (GenericYSelector.SelectionMode selectionMode : GenericYSelector.SelectionMode.values())
        {
            String transKeyBase = "structures.genY." + selectionMode.serializedName();
            generationBases.add(new TableCellEnum.Option<>(selectionMode,
                    I18n.format(transKeyBase), IvTranslations.formatLines(transKeyBase + ".tooltip")));
        }

        return generationBases;
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
            TableCellEnum cell = new TableCellEnum<>("ySelType", ySelector.selectionMode, allGenerationOptions());
            cell.addPropertyListener(this);
            return new TableElementCell("Generation Base", cell);
        }
        else if (index == 1)
        {
            TableCellIntegerRange cell = new TableCellIntegerRange("ySelShift", new IntegerRange(ySelector.minYShift, ySelector.maxYShift), -100, 100);
            cell.setTooltip(IvTranslations.formatLines("reccomplex.structure.randomrange"));
            cell.addPropertyListener(this);
            return new TableElementCell("Y Shift", cell);
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
