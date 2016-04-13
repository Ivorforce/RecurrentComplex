/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.utils.IvTranslations;
import ivorius.reccomplex.utils.scale.Scales;

/**
 * Created by lukas on 13.04.16.
 */
public class RCGuiTables
{
    public static TableElement defaultWeightElement(TableCellPropertyListener listener, Double value)
    {
        TableCellFloatNullable cell = new TableCellFloatNullable("value", TableElements.toFloat(value), 1.0f, 0, 1000, "D", "C");
        cell.setScale(Scales.pow(5));
        cell.addPropertyListener(listener);
        cell.setTooltip(IvTranslations.formatLines("structures.gui.random.value.tooltip"));
        return new TableElementCell(IvTranslations.get("structures.gui.random.value"), cell);
    }
}
