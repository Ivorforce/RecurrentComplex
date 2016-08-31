/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import ivorius.reccomplex.gui.table.*;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.utils.scale.Scales;

import java.util.function.Consumer;

/**
 * Created by lukas on 13.04.16.
 */
public class RCGuiTables
{
    public static TableElement defaultWeightElement(Consumer<Float> consumer, Float value)
    {
        TableCellFloatNullable cell = new TableCellFloatNullable("value", value, 1.0f, 0, 1000, "D", "C");
        cell.setScale(Scales.pow(5));
        cell.addPropertyConsumer(consumer);
        cell.setTooltip(IvTranslations.formatLines("reccomplex.gui.random.weight.tooltip"));
        return new TableElementCell(IvTranslations.get("reccomplex.gui.random.weight"), cell);
    }

    public static TableElement defaultWeightElement(Consumer<Float> listener, Double value)
    {
        return defaultWeightElement(listener, TableElements.toFloat(value));
    }
}
