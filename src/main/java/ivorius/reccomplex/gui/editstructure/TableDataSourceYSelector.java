/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui.editstructure;

import ivorius.ivtoolkit.gui.IntegerRange;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.structures.generic.GenerationYSelector;
import ivorius.reccomplex.utils.IvTranslations;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukas on 04.04.15.
 */
public class TableDataSourceYSelector implements TableDataSource, TableElementPropertyListener
{
    public GenerationYSelector ySelector;

    public TableDataSourceYSelector(GenerationYSelector ySelector)
    {
        this.ySelector = ySelector;
    }

    public static List<TableElementList.Option> allGenerationOptions()
    {
        List<TableElementList.Option> generationBases = new ArrayList<>();

        for (GenerationYSelector.SelectionMode selectionMode : GenerationYSelector.SelectionMode.values())
        {
            String transKeyBase = "structures.genY." + selectionMode.serializedName();
            generationBases.add(new TableElementList.Option(selectionMode.serializedName(),
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
            TableElementList element = new TableElementList("ySelType", "Generation Base", ySelector.selectionMode.serializedName(), allGenerationOptions());
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
                GenerationYSelector.SelectionMode selectionMode = GenerationYSelector.SelectionMode.selectionMode((String) element.getPropertyValue());
                ySelector.selectionMode = selectionMode != null ? selectionMode : GenerationYSelector.SelectionMode.SURFACE;
                break;
            case "ySelShift":
                IntegerRange range = ((IntegerRange) element.getPropertyValue());
                ySelector.minY = range.getMin();
                ySelector.maxY = range.getMax();
                break;
        }
    }
}
