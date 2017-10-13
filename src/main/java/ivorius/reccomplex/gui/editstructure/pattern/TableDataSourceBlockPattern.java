/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.pattern;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.client.rendering.MazeVisualizationContext;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellMultiBuilder;
import ivorius.reccomplex.gui.table.cell.TableCellTitle;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSupplied;
import ivorius.reccomplex.gui.worldscripts.mazegenerator.TableDataSourceSelection;
import ivorius.reccomplex.world.gen.feature.structure.generic.BlockPattern;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 22.02.15.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceBlockPattern extends TableDataSourceSegmented
{
    protected BlockPattern pattern;

    public TableDataSourceBlockPattern(BlockPattern pattern, MazeVisualizationContext visualizationContext, TableDelegate delegate, TableNavigator navigator)
    {
        this.pattern = pattern;

        addSegment(0, TableCellMultiBuilder.create(navigator, delegate)
                .addNavigation(() -> new TableDataSourceSelection(pattern.pattern, new int[]{50, 50, 50}, delegate, navigator, true)
                        .visualizing(visualizationContext)).withTitle(IvTranslations.get("reccomplex.blockpattern.pattern"), IvTranslations.getLines("reccomplex.blockpattern.pattern.tooltip")).buildDataSource());
        addSegment(1, new TableDataSourceSupplied(() -> new TitledCell(
                new TableCellTitle(null, IvTranslations.get("reccomplex.blockpattern.ingredients")).withTooltip(IvTranslations.formatLines("reccomplex.blockpattern.ingredients.tooltip")))));
        addSegment(2, new TableDataSourceBlockPatternIngredientList(pattern.ingredients, delegate, navigator));
    }
}
