/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.gentypes;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.editstructure.placer.TableDataSourcePlacer;
import ivorius.reccomplex.gui.table.GuiTable;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCell;
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

    public TableDataSourceSelectivePlacer(TableNavigator navigator, TableDelegate delegate, SelectivePlacer placer)
    {
        this.navigator = navigator;
        this.tableDelegate = delegate;
        this.placer = placer;

        addManagedSegment(0, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourcePlacer(placer.placer, delegate, navigator))
                .buildDataSource(IvTranslations.get("reccomplex.placer"), IvTranslations.getLines("reccomplex.placer.tooltip")));
    }

    @Override
    public int numberOfSegments()
    {
        return 2;
    }

    @Override
    public int sizeOfSegment(int segment)
    {
        return segment == 1 ? 1 : super.sizeOfSegment(segment);
    }

    @Override
    public TableCell cellForIndexInSegment(GuiTable table, int index, int segment)
    {
        if (segment == 1)
        {
            TableCellStringInt cell = new TableCellStringInt(null, placer.baseline);
            cell.addListener(i -> placer.baseline = i);
            return new TitledCell(IvTranslations.get("reccomplex.placer.baseline"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.placer.baseline.tooltip"));
        }

        return super.cellForIndexInSegment(table, index, segment);
    }
}
