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
public class TableDataSourceYSelector implements TableDataSource, TableElementPropertyListener
{
    public GenericYSelector ySelector;

    public TableDataSourceYSelector(GenericYSelector ySelector)
    {
        this.ySelector = ySelector;
    }

    public static List<TableElementEnum.Option<GenericYSelector.SelectionMode>> allGenerationOptions()
    {
        List<TableElementEnum.Option<GenericYSelector.SelectionMode>> generationBases = new ArrayList<>();

        for (GenericYSelector.SelectionMode selectionMode : GenericYSelector.SelectionMode.values())
        {
            String transKeyBase = "structures.genY." + selectionMode.serializedName();
            generationBases.add(new TableElementEnum.Option<>(selectionMode,
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
            TableElementEnum element = new TableElementEnum<>("ySelType", "Generation Base", ySelector.selectionMode, allGenerationOptions());
            element.addPropertyListener(this);
            return element;
        }
        else if (index == 1)
        {
            TableElementIntegerRange element = new TableElementIntegerRange("ySelShift", "Y Shift", new IntegerRange(ySelector.minY, ySelector.maxY), -100, 100);
            element.setTooltip(IvTranslations.formatLines("reccomplex.structure.randomrange"));
            element.addPropertyListener(this);
            return element;
        }

        return null;
    }

    @Override
    public void valueChanged(TableElementPropertyDefault element)
    {
        switch (element.getID())
        {
            case "ySelType":
                ySelector.selectionMode = (GenericYSelector.SelectionMode) element.getPropertyValue();
                break;
            case "ySelShift":
                IntegerRange range = ((IntegerRange) element.getPropertyValue());
                ySelector.minY = range.getMin();
                ySelector.maxY = range.getMax();
                break;
        }
    }
}
