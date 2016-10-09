/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.pattern;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.table.cell.TableCellMultiBuilder;
import ivorius.reccomplex.gui.table.cell.TableCellTitle;
import ivorius.reccomplex.gui.table.cell.TableElementCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSupplied;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.TableDataSourceSelection;
import ivorius.reccomplex.world.gen.feature.structure.generic.BlockPattern;

/**
 * Created by lukas on 22.02.15.
 */
public class TableDataSourceBlockPattern extends TableDataSourceSegmented
{
    protected BlockPattern pattern;

    public TableDataSourceBlockPattern(BlockPattern pattern, TableDelegate delegate, TableNavigator navigator)
    {
        this.pattern = pattern;

        addManagedSegment(0, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceSelection(pattern.pattern, new int[]{50, 50, 50}, delegate, navigator, true))
                .buildDataSource(IvTranslations.get("reccomplex.blockpattern.pattern"), IvTranslations.getLines("reccomplex.blockpattern.pattern.tooltip")));
        addManagedSegment(1, new TableDataSourceSupplied(() -> new TableElementCell(
                new TableCellTitle(null, IvTranslations.get("reccomplex.blockpattern.ingredients")).withTooltip(IvTranslations.formatLines("reccomplex.blockpattern.ingredients")))));
        addManagedSegment(2, new TableDataSourceBlockPatternIngredientList(pattern.ingredients, delegate, navigator));
    }
}
