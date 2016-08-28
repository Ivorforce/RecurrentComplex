/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import ivorius.reccomplex.gui.table.*;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.utils.scale.Scales;

/**
 * Created by lukas on 13.04.16.
 */
public class RCGuiTables
{
    public static TableElement defaultWeightElement(TableCellPropertyListener listener, Float value)
    {
        TableCellFloatNullable cell = new TableCellFloatNullable("value", value, 1.0f, 0, 1000, "D", "C");
        cell.setScale(Scales.pow(5));
        cell.addPropertyListener(listener);
        cell.setTooltip(IvTranslations.formatLines("reccomplex.gui.random.weight.tooltip"));
        return new TableElementCell(IvTranslations.get("reccomplex.gui.random.weight"), cell);
    }

    public static TableElement defaultWeightElement(TableCellPropertyListener listener, Double value)
    {
        return defaultWeightElement(listener, TableElements.toFloat(value));
    }
}
