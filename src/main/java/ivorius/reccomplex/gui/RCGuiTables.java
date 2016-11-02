/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.reccomplex.gui;

import ivorius.reccomplex.gui.table.*;
import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.cell.TableCellFloatNullable;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.utils.scale.Scales;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by lukas on 13.04.16.
 */
public class RCGuiTables
{
    public static TitledCell defaultWeightElement(Consumer<Float> consumer, Float value, String title, @Nullable List<String> tooltip)
    {
        TableCellFloatNullable cell = new TableCellFloatNullable("value", value, 1.0f, 0, 1000, IvTranslations.get("reccomplex.gui.random.weight.default.short"), IvTranslations.get("reccomplex.gui.random.weight.custom.short"));
        cell.setScale(Scales.pow(5));
        cell.addPropertyConsumer(consumer);
        return new TitledCell(title, cell).withTitleTooltip(tooltip);
    }

    public static TitledCell defaultWeightElement(Consumer<Float> consumer, Double value, String title, @Nullable List<String> tooltip)
    {
        return defaultWeightElement(consumer, TableCells.toFloat(value), title, tooltip);
    }

    public static TitledCell defaultWeightElement(Consumer<Float> consumer, Float value)
    {
        return defaultWeightElement(consumer, value, IvTranslations.get("reccomplex.gui.random.weight"), IvTranslations.formatLines("reccomplex.gui.random.weight.tooltip"));
    }

    public static TitledCell defaultWeightElement(Consumer<Float> listener, Double value)
    {
        return defaultWeightElement(listener, TableCells.toFloat(value));
    }
}
