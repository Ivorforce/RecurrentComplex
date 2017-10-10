/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.placer;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCellFloat;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSupplied;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.GenericPlacer;

import javax.annotation.Nonnull;

/**
 * Created by lukas on 04.04.15.
 */
public class TableDataSourcePlacerFactor extends TableDataSourceSegmented
{
    public GenericPlacer.Factor placer;

    public TableDataSourcePlacerFactor(GenericPlacer.Factor factor, TableDelegate delegate, TableNavigator navigator)
    {
        this.placer = factor;

        addManagedSegment(0, new TableDataSourceSupplied(() -> {
            TableCellFloat priority = new TableCellFloat(null, factor.priority, 0, 10);
            priority.addListener(v -> factor.priority = v);
            return new TitledCell(IvTranslations.get("reccomplex.placer.factor.priority"), priority)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.placer.factor.priority.tooltip"));
        }));
    }

    @Nonnull
    @Override
    public String title()
    {
        return placer.displayString();
    }
}
