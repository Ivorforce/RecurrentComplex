/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://ivorius.net
 */

package ivorius.reccomplex.gui.editstructure.placer;

import ivorius.ivtoolkit.tools.IvTranslations;
import ivorius.reccomplex.gui.TableDataSourceExpression;
import ivorius.reccomplex.gui.table.TableDelegate;
import ivorius.reccomplex.gui.table.TableNavigator;
import ivorius.reccomplex.gui.table.cell.TableCellFloatSlider;
import ivorius.reccomplex.gui.table.cell.TitledCell;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSegmented;
import ivorius.reccomplex.gui.table.datasource.TableDataSourceSupplied;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.FactorMatch;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by lukas on 04.04.15.
 */

@SideOnly(Side.CLIENT)
public class TableDataSourceFactorMatch extends TableDataSourceSegmented
{
    public FactorMatch placer;

    public TableDataSourceFactorMatch(FactorMatch factor, TableDelegate delegate, TableNavigator navigator)
    {
        this.placer = factor;

        addSegment(0, new TableDataSourcePlacerFactor(factor, delegate, navigator));
        addSegment(1, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.gui.sources"), factor.sourceMatcher, null));
        addSegment(2, TableDataSourceExpression.constructDefault(IvTranslations.get("reccomplex.placer.factors.match.dest"), factor.destMatcher, null));
        addSegment(3, new TableDataSourceSupplied(() -> {
            TableCellFloatSlider cell = new TableCellFloatSlider(null, factor.requiredConformity, 0, 1);
            cell.addListener(v -> factor.requiredConformity = v);
            return new TitledCell(IvTranslations.get("reccomplex.placer.factors.match.conformity"), cell)
                    .withTitleTooltip(IvTranslations.getLines("reccomplex.placer.factors.match.conformity.tooltip"));
        }));
    }
}
