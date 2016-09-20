/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.pattern;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.table.*;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.TableDataSourceSelection;
import ivorius.reccomplex.structures.generic.BlockPattern;

/**
 * Created by lukas on 22.02.15.
 */
public class TableDataSourceBlockPattern extends TableDataSourceSegmented
{
    protected BlockPattern pattern;

    public TableDataSourceBlockPattern(BlockPattern pattern, TableDelegate delegate, TableNavigator navigator)
    {
        this.pattern = pattern;

        addManagedSection(0, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceSelection(pattern.pattern, new int[]{50, 50, 50}, delegate, navigator, true))
                .buildDataSource(IvTranslations.get("reccomplex.blockpattern.pattern"), IvTranslations.getLines("reccomplex.blockpattern.pattern.tooltip")));
        addManagedSection(1, new TableDataSourceBlockPatternIngredientList(pattern.ingredients, delegate, navigator));
    }
}
