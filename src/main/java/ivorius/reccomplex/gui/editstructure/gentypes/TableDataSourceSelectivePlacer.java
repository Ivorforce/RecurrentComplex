/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.editstructure.placer.TableDataSourcePlacer;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellMultiBuilder;
import ivorius.reccomplex.gui.table.cell.TableCellStringInt;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.SelectivePlacer;

public class TableDataSourceSelectivePlacer extends TableDataSourceSegmented
{
    protected TableNavigator navigator;
    protected TableDelegate tableDelegate;

    protected SelectivePlacer placer;

    public TableDataSourceSelectivePlacer(SelectivePlacer placer, TableNavigator navigator, TableDelegate delegate)
    {
        this.navigator = navigator;
        this.tableDelegate = delegate;
        this.placer = placer;

        addSegment(0, () -> {
            TableCellStringInt cell = new TableCellStringInt(null, placer.baseline);
            cell.addListener(i -> placer.baseline = i);
            return new TitledCell(IvTranslations.get("reccomplex.placer.baseline"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.placer.baseline.tooltip"));
        });

        addSegment(1, new TableDataSourcePlacer(placer.placer, delegate, navigator));
    }
}
